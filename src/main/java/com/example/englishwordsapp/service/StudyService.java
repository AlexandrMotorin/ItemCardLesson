package com.example.englishwordsapp.service;

import com.example.englishwordsapp.model.StudyResult;
import com.example.englishwordsapp.model.StudySession;
import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.repository.StudyResultRepository;
import com.example.englishwordsapp.repository.StudySessionRepository;
import com.example.englishwordsapp.repository.UserRepository;
import com.example.englishwordsapp.repository.WordCardRepository;
import com.example.englishwordsapp.repository.WordSetRepository;
import com.example.englishwordsapp.security.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudySessionRepository studySessionRepository;
    private final StudyResultRepository studyResultRepository;
    private final WordCardRepository wordCardRepository;
    private final WordSetRepository wordSetRepository;
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;

    @Transactional
    public StudySession startSession(Long userId, List<Long> setIds, String exerciseTypeStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        StudySession.ExerciseType exerciseType = StudySession.ExerciseType.valueOf(exerciseTypeStr);

        // Collect all words from selected sets
        List<WordCard> allWords = new ArrayList<>();
        for (Long setId : setIds) {
            WordSet wordSet = wordSetRepository.findById(setId)
                    .orElseThrow(() -> new RuntimeException("WordSet not found with id: " + setId));
            allWords.addAll(wordSet.getWordCards());
        }

        // Remove duplicates by card ID
        allWords = allWords.stream()
                .distinct()
                .collect(Collectors.toList());

        // Shuffle randomly
        Collections.shuffle(allWords, new Random());

        // Build comma-separated list of word card IDs
        String wordIdsOrder = allWords.stream()
                .map(card -> card.getId().toString())
                .collect(Collectors.joining(","));

        StudySession session = new StudySession();
        session.setUser(user);
        session.setExerciseType(exerciseType);
        session.setWordCount(allWords.size());
        session.setCorrectCount(0);
        session.setCurrentIndex(0);
        session.setWordIdsOrder(wordIdsOrder);

        return studySessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public StudySession getSession(Long sessionId) {
        return studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("StudySession not found with id: " + sessionId));
    }

    @Transactional(readOnly = true)
    public WordCard getCurrentWord(Long sessionId) {
        StudySession session = getSession(sessionId);
        Long wordId = parseWordIds(session.getWordIdsOrder(), session.getCurrentIndex());
        return wordCardRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordId));
    }

    @Transactional(readOnly = true)
    public WordCard getNextWord(Long sessionId) {
        StudySession session = getSession(sessionId);
        int nextIndex = session.getCurrentIndex() + 1;
        if (nextIndex >= session.getWordCount()) {
            return null; // No more words
        }
        session.setCurrentIndex(nextIndex);
        studySessionRepository.save(session);

        Long wordId = parseWordIds(session.getWordIdsOrder(), nextIndex);
        return wordCardRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordId));
    }

    @Transactional
    public StudyResult submitAnswer(Long sessionId, Long wordCardId, String userAnswer, String exerciseTypeStr) {
        StudySession session = getSession(sessionId);
        WordCard wordCard = wordCardRepository.findById(wordCardId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordCardId));
        WordSet wordSet = findWordSetForWord(wordCardId);

        StudyResult.ExerciseType exerciseType = StudyResult.ExerciseType.valueOf(exerciseTypeStr);

        boolean isCorrect;
        if (exerciseType == StudyResult.ExerciseType.TEXT_INPUT) {
            // For TEXT_INPUT, compare lowercased and trimmed
            isCorrect = userAnswer != null
                    && wordCard.getTranslation().trim().equalsIgnoreCase(userAnswer.trim());
        } else {
            // For MULTIPLE_CHOICE, userAnswer contains the word card ID of the selected answer
            isCorrect = userAnswer != null && userAnswer.equals(String.valueOf(wordCardId));
        }

        StudyResult result = new StudyResult();
        result.setStudySession(session);
        result.setWordCard(wordCard);
        result.setWordSet(wordSet);
        result.setCorrect(isCorrect);
        result.setExerciseType(exerciseType);
        result.setAnsweredAt(LocalDateTime.now());
        studyResultRepository.save(result);

        // Update progress in analytics
        if (wordSet != null) {
            analyticsService.updateProgress(session.getUser().getId(), wordCardId, wordSet.getId(), isCorrect);
        }

        // Update session correct count
        if (isCorrect) {
            session.setCorrectCount(session.getCorrectCount() + 1);
        }

        // Auto-advance to next word
        int nextIndex = session.getCurrentIndex() + 1;
        if (nextIndex < session.getWordCount()) {
            session.setCurrentIndex(nextIndex);
        }

        studySessionRepository.save(session);

        return result;
    }

    @Transactional(readOnly = true)
    public List<WordCard> generateChoices(Long correctWordCardId, List<Long> setIds, int count) {
        wordCardRepository.findById(correctWordCardId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + correctWordCardId));

        // Collect all words from the same sets (excluding the correct one)
        List<WordCard> allWords = new ArrayList<>();
        for (Long setId : setIds) {
            WordSet wordSet = wordSetRepository.findById(setId)
                    .orElseThrow(() -> new RuntimeException("WordSet not found with id: " + setId));
            for (WordCard card : wordSet.getWordCards()) {
                if (!card.getId().equals(correctWordCardId) && !allWords.contains(card)) {
                    allWords.add(card);
                }
            }
        }

        // Shuffle and pick distractors
        Collections.shuffle(allWords, new Random());
        List<WordCard> choices = new ArrayList<>();
        choices.add(wordCardRepository.findById(correctWordCardId).orElseThrow());
        for (int i = 0; i < count - 1 && i < allWords.size(); i++) {
            choices.add(allWords.get(i));
        }

        // Shuffle to randomize position of correct answer
        Collections.shuffle(choices, new Random());

        return choices;
    }

    @Transactional
    public StudySession endSession(Long sessionId) {
        StudySession session = getSession(sessionId);
        session.setEndedAt(LocalDateTime.now());
        return studySessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<StudyResult> getSessionResults(Long sessionId) {
        return studyResultRepository.findByStudySessionId(sessionId);
    }

    private Long parseWordIds(String wordIdsOrder, int index) {
        if (wordIdsOrder == null || wordIdsOrder.isEmpty()) {
            throw new RuntimeException("No words in session");
        }
        String[] ids = wordIdsOrder.split(",");
        if (index < 0 || index >= ids.length) {
            throw new RuntimeException("Invalid index: " + index);
        }
        return Long.parseLong(ids[index].trim());
    }

    private WordSet findWordSetForWord(Long wordCardId) {
        return wordCardRepository.findById(wordCardId)
                .flatMap(wordCard -> wordCard.getWordSets().stream().findFirst())
                .orElse(null);
    }
}

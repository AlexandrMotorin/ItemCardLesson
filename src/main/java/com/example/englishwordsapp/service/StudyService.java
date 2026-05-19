package com.example.englishwordsapp.service;

import com.example.englishwordsapp.dto.QuestionDto;
import com.example.englishwordsapp.model.Direction;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private static final int CHOICES_COUNT = 4;
    private static final Pattern PUNCTUATION = Pattern.compile("[.,!?;:]");

    private final StudySessionRepository studySessionRepository;
    private final StudyResultRepository studyResultRepository;
    private final WordCardRepository wordCardRepository;
    private final WordSetRepository wordSetRepository;
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;
    private final Random random = new Random();

    @Transactional
    public StudySession startSession(Long userId, List<Long> setIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Collect all words from selected sets
        List<WordCard> allWords = new ArrayList<>();
        for (Long setId : setIds) {
            WordSet wordSet = wordSetRepository.findById(setId)
                    .orElseThrow(() -> new RuntimeException("WordSet not found with id: " + setId));
            allWords.addAll(wordSet.getWordCards());
        }

        // Remove duplicates and shuffle
        allWords = allWords.stream().distinct().collect(Collectors.toList());
        Collections.shuffle(allWords, random);

        // Build comma-separated word IDs
        String wordIdsOrder = allWords.stream()
                .map(card -> card.getId().toString())
                .collect(Collectors.joining(","));

        // Generate per-question exerciseType (50/50) and direction (50/50)
        StudyResult.ExerciseType[] exerciseTypes = StudyResult.ExerciseType.values();
        Direction[] directions = Direction.values();

        String exerciseTypesOrder = allWords.stream()
                .map(w -> exerciseTypes[random.nextInt(exerciseTypes.length)].name())
                .collect(Collectors.joining(","));

        String directionsOrder = allWords.stream()
                .map(w -> directions[random.nextInt(directions.length)].name())
                .collect(Collectors.joining(","));

        StudySession session = new StudySession();
        session.setUser(user);
        session.setExerciseType(StudySession.ExerciseType.MIXED);
        session.setWordCount(allWords.size());
        session.setCorrectCount(0);
        session.setCurrentIndex(0);
        session.setWordIdsOrder(wordIdsOrder);
        session.setExerciseTypesOrder(exerciseTypesOrder);
        session.setDirectionsOrder(directionsOrder);

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
        Long wordId = parseCommaSeparatedLong(session.getWordIdsOrder(), session.getCurrentIndex());
        return wordCardRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordId));
    }

    @Transactional(readOnly = true)
    public QuestionDto getCurrentQuestionInfo(Long sessionId) {
        StudySession session = getSession(sessionId);
        int idx = session.getCurrentIndex();

        Long wordId = parseCommaSeparatedLong(session.getWordIdsOrder(), idx);
        WordCard wordCard = wordCardRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordId));

        StudyResult.ExerciseType exerciseType = getExerciseTypeForIndex(session, idx);
        Direction direction = getDirectionForIndex(session, idx);

        List<WordCard> choices = null;
        if (exerciseType == StudyResult.ExerciseType.MULTIPLE_CHOICE) {
            choices = generateChoices(session, wordCard.getId());
        }

        return QuestionDto.builder()
                .wordCard(wordCard)
                .exerciseType(exerciseType)
                .direction(direction)
                .currentIndex(idx)
                .totalWords(session.getWordCount())
                .choices(choices)
                .build();
    }

    @Transactional(readOnly = true)
    public WordCard getNextWord(Long sessionId) {
        StudySession session = getSession(sessionId);
        int nextIndex = session.getCurrentIndex() + 1;
        if (nextIndex >= session.getWordCount()) {
            return null;
        }
        session.setCurrentIndex(nextIndex);
        studySessionRepository.save(session);

        Long wordId = parseCommaSeparatedLong(session.getWordIdsOrder(), nextIndex);
        return wordCardRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordId));
    }

    @Transactional
    public StudyResult submitAnswer(Long sessionId, Long wordCardId, String userAnswer, Direction direction) {
        StudySession session = getSession(sessionId);
        WordCard wordCard = wordCardRepository.findById(wordCardId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordCardId));
        WordSet wordSet = findWordSetForWord(wordCardId);

        StudyResult.ExerciseType exerciseType = getExerciseTypeForIndex(session, session.getCurrentIndex());

        boolean isCorrect;
        if (exerciseType == StudyResult.ExerciseType.TEXT_INPUT) {
            String expected = (direction == Direction.EN_TO_RU)
                    ? wordCard.getTranslation()
                    : wordCard.getEnglishWord();
            isCorrect = userAnswer != null && normalizeAnswer(expected).equals(normalizeAnswer(userAnswer));
        } else {
            // MULTIPLE_CHOICE: userAnswer contains the selected word card ID
            isCorrect = userAnswer != null && userAnswer.equals(String.valueOf(wordCardId));
        }

        StudyResult result = new StudyResult();
        result.setStudySession(session);
        result.setWordCard(wordCard);
        result.setWordSet(wordSet);
        result.setCorrect(isCorrect);
        result.setExerciseType(exerciseType);
        result.setAnsweredAt(LocalDateTime.now());
        result.setUserAnswer(userAnswer);
        result.setDirection(direction != null ? direction.name() : null);
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

    /**
     * Генерация вариантов ответа для MULTIPLE_CHOICE из слов текущей сессии.
     */
    private List<WordCard> generateChoices(StudySession session, Long correctWordCardId) {
        String[] allIds = session.getWordIdsOrder().split(",");
        List<WordCard> distractors = new ArrayList<>();

        for (String idStr : allIds) {
            Long id = Long.parseLong(idStr.trim());
            if (!id.equals(correctWordCardId)) {
                wordCardRepository.findById(id).ifPresent(distractors::add);
            }
        }

        Collections.shuffle(distractors, random);

        List<WordCard> choices = new ArrayList<>();
        choices.add(wordCardRepository.findById(correctWordCardId).orElseThrow());
        for (int i = 0; i < CHOICES_COUNT - 1 && i < distractors.size(); i++) {
            choices.add(distractors.get(i));
        }

        Collections.shuffle(choices, random);
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

    // --- Helper methods ---

    private StudyResult.ExerciseType getExerciseTypeForIndex(StudySession session, int index) {
        String value = parseCommaSeparatedString(session.getExerciseTypesOrder(), index);
        return StudyResult.ExerciseType.valueOf(value);
    }

    private Direction getDirectionForIndex(StudySession session, int index) {
        String value = parseCommaSeparatedString(session.getDirectionsOrder(), index);
        return Direction.valueOf(value);
    }

    private Long parseCommaSeparatedLong(String csv, int index) {
        if (csv == null || csv.isEmpty()) {
            throw new RuntimeException("No data in session");
        }
        String[] parts = csv.split(",");
        if (index < 0 || index >= parts.length) {
            throw new RuntimeException("Invalid index: " + index);
        }
        return Long.parseLong(parts[index].trim());
    }

    private String parseCommaSeparatedString(String csv, int index) {
        if (csv == null || csv.isEmpty()) {
            throw new RuntimeException("No data in session");
        }
        String[] parts = csv.split(",");
        if (index < 0 || index >= parts.length) {
            throw new RuntimeException("Invalid index: " + index);
        }
        return parts[index].trim();
    }

    private String normalizeAnswer(String answer) {
        return PUNCTUATION.matcher(answer.toLowerCase().trim()).replaceAll("");
    }

    private WordSet findWordSetForWord(Long wordCardId) {
        return wordCardRepository.findById(wordCardId)
                .flatMap(wordCard -> wordCard.getWordSets().stream().findFirst())
                .orElse(null);
    }
}

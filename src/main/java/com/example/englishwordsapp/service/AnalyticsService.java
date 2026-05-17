package com.example.englishwordsapp.service;

import com.example.englishwordsapp.model.UserWordProgress;
import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.repository.StudyResultRepository;
import com.example.englishwordsapp.repository.UserWordProgressRepository;
import com.example.englishwordsapp.repository.WordCardRepository;
import com.example.englishwordsapp.repository.WordSetRepository;
import com.example.englishwordsapp.security.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsService {

    private final UserWordProgressRepository progressRepository;
    private final WordSetRepository wordSetRepository;
    private final WordCardRepository wordCardRepository;
    private final StudyResultRepository studyResultRepository;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SetStats {
        private int totalWords;
        private int totalAttempts;
        private int correctAnswers;
        private double successRate;
        private int wordsLearned;
        private int wordsInProgress;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WordStats {
        private int totalAttempts;
        private int correctAnswers;
        private double successRate;
        private int textInputAttempts;
        private int textInputCorrect;
        private int multipleChoiceAttempts;
        private int multipleChoiceCorrect;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SetSummary {
        private Long wordSetId;
        private String setName;
        private int totalWords;
        private int totalAttempts;
        private double successRate;
    }

    @Transactional(readOnly = true)
    public SetStats getSetStats(Long userId, Long wordSetId) {
        WordSet wordSet = wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new RuntimeException("WordSet not found with id: " + wordSetId));

        List<UserWordProgress> progresses = progressRepository.findByUserIdAndWordSetId(userId, wordSetId);
        int totalWords = wordSet.getWordCards().size();

        int totalAttempts = progresses.stream().mapToInt(UserWordProgress::getTotalAttempts).sum();
        int correctAnswers = progresses.stream().mapToInt(UserWordProgress::getCorrectAnswers).sum();
        double successRate = totalAttempts == 0 ? 0.0 : (double) correctAnswers / totalAttempts * 100;

        int wordsLearned = (int) progresses.stream().filter(p -> {
            int threshold = 3;
            return p.getTotalAttempts() >= threshold && p.getCorrectAnswers() >= threshold;
        }).count();

        int wordsInProgress = (int) progresses.stream().filter(p -> {
            int threshold = 3;
            return p.getTotalAttempts() > 0 && p.getTotalAttempts() < threshold;
        }).count();

        return new SetStats(totalWords, totalAttempts, correctAnswers, successRate, wordsLearned, wordsInProgress);
    }

    @Transactional(readOnly = true)
    public WordStats getWordStats(Long userId, Long wordCardId, Long wordSetId) {
        Optional<UserWordProgress> optProgress = progressRepository
                .findByUserIdAndWordCardIdAndWordSetId(userId, wordCardId, wordSetId);

        int totalAttempts = 0;
        int correctAnswers = 0;

        if (optProgress.isPresent()) {
            UserWordProgress progress = optProgress.get();
            totalAttempts = progress.getTotalAttempts();
            correctAnswers = progress.getCorrectAnswers();
        }

        double successRate = totalAttempts == 0 ? 0.0 : (double) correctAnswers / totalAttempts * 100;

        Object[] textInputStats = studyResultRepository.getExerciseTypeStats(
                userId, wordCardId, wordSetId, "TEXT_INPUT");
        Object[] multipleChoiceStats = studyResultRepository.getExerciseTypeStats(
                userId, wordCardId, wordSetId, "MULTIPLE_CHOICE");

        int textInputAttempts = 0, textInputCorrect = 0;
        int multipleChoiceAttempts = 0, multipleChoiceCorrect = 0;

        if (textInputStats != null && textInputStats[0] != null) {
            textInputAttempts = ((Number) textInputStats[0]).intValue();
            textInputCorrect = textInputStats[1] != null ? ((Number) textInputStats[1]).intValue() : 0;
        }

        if (multipleChoiceStats != null && multipleChoiceStats[0] != null) {
            multipleChoiceAttempts = ((Number) multipleChoiceStats[0]).intValue();
            multipleChoiceCorrect = multipleChoiceStats[1] != null ? ((Number) multipleChoiceStats[1]).intValue() : 0;
        }

        return new WordStats(totalAttempts, correctAnswers, successRate,
                textInputAttempts, textInputCorrect,
                multipleChoiceAttempts, multipleChoiceCorrect);
    }

    @Transactional(readOnly = true)
    public List<SetSummary> getAllSetsStats(Long userId) {
        List<WordSet> userSets = wordSetRepository.findByOwnerId(userId);
        List<SetSummary> summaries = new ArrayList<>();

        for (WordSet set : userSets) {
            List<UserWordProgress> progresses = progressRepository.findByUserIdAndWordSetId(userId, set.getId());
            int wordCount = set.getWordCards().size();
            int totalAttempts = progresses.stream().mapToInt(UserWordProgress::getTotalAttempts).sum();
            int correctAnswers = progresses.stream().mapToInt(UserWordProgress::getCorrectAnswers).sum();
            double successRate = totalAttempts == 0 ? 0.0 : (double) correctAnswers / totalAttempts * 100;

            summaries.add(new SetSummary(set.getId(), set.getName(), wordCount, totalAttempts, successRate));
        }

        return summaries;
    }

    @Transactional
    public void updateProgress(Long userId, Long wordCardId, Long wordSetId, boolean isCorrect) {
        LocalDateTime now = LocalDateTime.now();

        UserWordProgress progress = progressRepository
                .findByUserIdAndWordCardIdAndWordSetId(userId, wordCardId, wordSetId)
                .orElseGet(() -> {
                    UserWordProgress newProgress = new UserWordProgress();

                    User userRef = new User();
                    userRef.setId(userId);
                    newProgress.setUser(userRef);

                    WordCard wordCardRef = new WordCard();
                    wordCardRef.setId(wordCardId);
                    newProgress.setWordCard(wordCardRef);

                    WordSet wordSetRef = new WordSet();
                    wordSetRef.setId(wordSetId);
                    newProgress.setWordSet(wordSetRef);

                    newProgress.setTotalAttempts(0);
                    newProgress.setCorrectAnswers(0);
                    return newProgress;
                });

        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
        if (isCorrect) {
            progress.setCorrectAnswers(progress.getCorrectAnswers() + 1);
        }
        progress.setLastPracticedAt(now);

        progressRepository.save(progress);
    }
}

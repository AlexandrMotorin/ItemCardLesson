package com.example.englishwordsapp.service;

import com.example.englishwordsapp.exception.SystemSetModificationException;
import com.example.englishwordsapp.exception.WordSetAccessDeniedException;
import com.example.englishwordsapp.exception.WordSetNotFoundException;
import com.example.englishwordsapp.model.*;
import com.example.englishwordsapp.repository.*;
import com.example.englishwordsapp.security.User;
import com.example.englishwordsapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WordSetService {

    private final WordSetRepository wordSetRepository;
    private final WordCardRepository wordCardRepository;
    private final UserRepository userRepository;
    private final UserSetSubscriptionRepository subscriptionRepository;
    private final WordSetForkRepository forkRepository;

    // ========== Reading ==========

    @Transactional(readOnly = true)
    public List<WordSet> getSystemSets() {
        return wordSetRepository.findByIsSystemTrue();
    }

    @Transactional(readOnly = true)
    public List<WordSet> getUserSets(Long userId) {
        return wordSetRepository.findByOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public List<WordSet> getAvailableSets(Long userId) {
        return wordSetRepository.findByOwnerIdOrIsSystemTrue(userId);
    }

    @Transactional(readOnly = true)
    public List<WordSet> getSubscribedSets(Long userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(UserSetSubscription::getWordSet)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WordCard> getSetWords(Long setId) {
        WordSet wordSet = wordSetRepository.findById(setId)
                .orElseThrow(() -> new WordSetNotFoundException(setId));
        return List.copyOf(wordSet.getWordCards());
    }

    // ========== CRUD ==========

    public WordSet createSet(Long userId, String name, String description) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        WordSet wordSet = new WordSet();
        wordSet.setName(name);
        wordSet.setDescription(description);
        wordSet.setOwner(owner);
        wordSet.setSystem(false);
        wordSet.setVisible(true);
        wordSet.setWordCards(new HashSet<>());

        return wordSetRepository.save(wordSet);
    }

    public WordSet createDefaultUserSet(Long userId) {
        return createSet(userId, "Мои словечки", "Your personal word collection");
    }

    /**
     * Найти или создать дефолтный набор пользователя "Мои словечки".
     */
    public WordSet getOrCreateDefaultUserSet(Long userId) {
        return wordSetRepository.findByNameAndOwnerId("Мои словечки", userId)
                .orElseGet(() -> createDefaultUserSet(userId));
    }

    public void deleteSet(Long setId, Long userId) {
        WordSet wordSet = wordSetRepository.findById(setId)
                .orElseThrow(() -> new WordSetNotFoundException(setId));

        if (wordSet.isSystem()) {
            throw new SystemSetModificationException();
        }
        if (wordSet.getOwner() == null || !wordSet.getOwner().getId().equals(userId)) {
            throw new WordSetAccessDeniedException("Not authorized to delete this set");
        }

        wordSetRepository.delete(wordSet);
    }

    // ========== Word Management ==========

    public void addWordToSet(Long setId, Long wordCardId, Long userId) {
        WordSet wordSet = wordSetRepository.findById(setId)
                .orElseThrow(() -> new WordSetNotFoundException(setId));
        validateCanModify(wordSet, userId);

        WordCard wordCard = wordCardRepository.findById(wordCardId)
                .orElseThrow(() -> new RuntimeException("WordCard not found"));

        wordSet.getWordCards().add(wordCard);
        wordSetRepository.save(wordSet);
    }

    public void removeWordFromSet(Long setId, Long wordCardId, Long userId) {
        WordSet wordSet = wordSetRepository.findById(setId)
                .orElseThrow(() -> new WordSetNotFoundException(setId));
        validateCanModify(wordSet, userId);

        WordCard wordCard = wordCardRepository.findById(wordCardId)
                .orElseThrow(() -> new RuntimeException("WordCard not found"));

        wordSet.getWordCards().remove(wordCard);
        wordSetRepository.save(wordSet);
    }

    // ========== Subscription ==========

    @Transactional
    public void subscribeToSet(Long userId, Long setId) {
        if (!subscriptionRepository.existsByUserIdAndWordSetId(userId, setId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            WordSet wordSet = wordSetRepository.findById(setId)
                    .orElseThrow(() -> new WordSetNotFoundException(setId));

            UserSetSubscription subscription = new UserSetSubscription();
            subscription.setUser(user);
            subscription.setWordSet(wordSet);
            subscription.setSubscribedAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    public void unsubscribeFromSet(Long userId, Long setId) {
        subscriptionRepository.deleteByUserIdAndWordSetId(userId, setId);
    }

    @Transactional(readOnly = true)
    public boolean isSubscribed(Long userId, Long setId) {
        return subscriptionRepository.existsByUserIdAndWordSetId(userId, setId);
    }

    // ========== Fork ==========

    @Transactional
    public WordSet forkSet(Long userId, Long originalSetId) {
        WordSet original = wordSetRepository.findById(originalSetId)
                .orElseThrow(() -> new WordSetNotFoundException(originalSetId));

        // Create new set with same name but owned by user
        WordSet forked = createSet(userId,
                original.getName() + " (fork)",
                original.getDescription());

        // Copy all words
        forked.setWordCards(new HashSet<>(original.getWordCards()));
        wordSetRepository.save(forked);

        // Record the fork
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WordSetFork fork = new WordSetFork();
        fork.setOriginalSet(original);
        fork.setForkedSet(forked);
        fork.setUser(user);
        fork.setForkedAt(LocalDateTime.now());
        forkRepository.save(fork);

        return forked;
    }

    // ========== Helpers ==========

    private void validateCanModify(WordSet wordSet, Long userId) {
        if (wordSet.isSystem()) {
            throw new SystemSetModificationException();
        }
        if (wordSet.getOwner() == null || !wordSet.getOwner().getId().equals(userId)) {
            throw new WordSetAccessDeniedException("Not authorized to modify this set");
        }
    }
}
package com.example.englishwordsapp.service;

import com.example.englishwordsapp.model.UserCard;
import com.example.englishwordsapp.model.UserCard.StudyStatus;
import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.repository.UserCardRepository;
import com.example.englishwordsapp.repository.UserRepository;
import com.example.englishwordsapp.repository.WordCardRepository;
import com.example.englishwordsapp.security.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WordCardService {

    private final WordCardRepository wordCardRepository;
    private final UserCardRepository userCardRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WordCard> getAllCards() {
        return wordCardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<WordCard> getCardById(Long id) {
        return wordCardRepository.findById(id);
    }

    public WordCard createCard(WordCard wordCard) {
        return wordCardRepository.save(wordCard);
    }

    public WordCard updateCard(Long id, WordCard wordCardDetails) {
        WordCard wordCard = wordCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Word card not found with id: " + id));

        wordCard.setEnglishWord(wordCardDetails.getEnglishWord());
        wordCard.setTranslation(wordCardDetails.getTranslation());
        wordCard.setExample(wordCardDetails.getExample());
        wordCard.setNotes(wordCardDetails.getNotes());
        wordCard.setDifficultyLevel(wordCardDetails.getDifficultyLevel());

        return wordCardRepository.save(wordCard);
    }

    public void deleteCard(Long id) {
        wordCardRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<WordCard> getCardsByDifficulty(WordCard.DifficultyLevel difficultyLevel) {
        return wordCardRepository.findByDifficultyLevel(difficultyLevel);
    }

    @Transactional(readOnly = true)
    public List<WordCard> searchCards(String query) {
        return wordCardRepository.findByEnglishWordContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public WordCard getRandomCard() {
        return wordCardRepository.findRandomCard().orElse(null);
    }

    // ========== User collection methods ==========

    @Transactional(readOnly = true)
    public List<WordCard> getUserCards(Long userId) {
        return userCardRepository.findByUserId(userId).stream()
                .map(UserCard::getWordCard)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Set<Long> getUserCardIds(Long userId) {
        return new HashSet<>(userCardRepository.findCardIdsByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<WordCard> getUserCardsByStatus(Long userId, StudyStatus status) {
        return userCardRepository.findByUserIdAndStatus(userId, status).stream()
                .map(UserCard::getWordCard)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserCard> getUserCardEntities(Long userId) {
        return userCardRepository.findByUserId(userId);
    }

    /**
     * Добавить слово в коллекцию пользователя.
     * Если слово уже добавлено — возвращает существующую запись.
     */
    public UserCard addCardToUserCollection(Long userId, Long cardId) {
        if (userCardRepository.existsByUserIdAndWordCardId(userId, cardId)) {
            return userCardRepository.findByUserIdAndWordCardId(userId, cardId).orElseThrow();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        WordCard wordCard = wordCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Word card not found with id: " + cardId));

        UserCard userCard = new UserCard();
        userCard.setUser(user);
        userCard.setWordCard(wordCard);
        userCard.setAddedAt(LocalDateTime.now());
        userCard.setStatus(StudyStatus.LEARNING);

        return userCardRepository.save(userCard);
    }

    /**
     * Удалить слово из коллекции пользователя.
     */
    public void removeCardFromUserCollection(Long userId, Long cardId) {
        userCardRepository.deleteByUserIdAndWordCardId(userId, cardId);
    }

    /**
     * Обновить статус изучения слова в коллекции пользователя.
     */
    public UserCard updateStudyStatus(Long userId, Long cardId, StudyStatus newStatus) {
        UserCard userCard = userCardRepository.findByUserIdAndWordCardId(userId, cardId)
                .orElseThrow(() -> new RuntimeException("UserCard not found for userId: " + userId + ", cardId: " + cardId));
        userCard.setStatus(newStatus);
        return userCardRepository.save(userCard);
    }

    @Transactional(readOnly = true)
    public boolean isCardInUserCollection(Long userId, Long cardId) {
        return userCardRepository.existsByUserIdAndWordCardId(userId, cardId);
    }

    @Transactional(readOnly = true)
    public Optional<UserCard> getUserCard(Long userId, Long cardId) {
        return userCardRepository.findByUserIdAndWordCardId(userId, cardId);
    }

    /**
     * Поиск по глобальному пулу слов для автодополнения.
     */
    @Transactional(readOnly = true)
    public List<WordCard> searchGlobalPool(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return wordCardRepository.findByEnglishWordContainingIgnoreCase(query.trim());
    }
}

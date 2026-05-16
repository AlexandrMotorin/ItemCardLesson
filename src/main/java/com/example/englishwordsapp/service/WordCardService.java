package com.example.englishwordsapp.service;

import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.repository.WordCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WordCardService {

    private final WordCardRepository wordCardRepository;

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
}

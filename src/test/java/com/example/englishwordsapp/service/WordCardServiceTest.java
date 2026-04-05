package com.example.englishwordsapp.service;

import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.repository.WordCardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WordCardServiceTest {

    @Autowired
    private WordCardService wordCardService;

    @Autowired
    private WordCardRepository wordCardRepository;

    @Test
    void shouldCreateWordCard() {
        WordCard wordCard = new WordCard();
        wordCard.setEnglishWord("Hello");
        wordCard.setTranslation("Привет");
        wordCard.setExample("Hello, how are you?");
        wordCard.setDifficultyLevel(WordCard.DifficultyLevel.BEGINNER);

        WordCard saved = wordCardService.createCard(wordCard);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEnglishWord()).isEqualTo("Hello");
        assertThat(saved.getTranslation()).isEqualTo("Привет");
    }

    @Test
    void shouldGetAllCards() {
        WordCard card1 = new WordCard(null, "Apple", "Яблоко", null, null, WordCard.DifficultyLevel.BEGINNER);
        WordCard card2 = new WordCard(null, "Book", "Книга", null, null, WordCard.DifficultyLevel.INTERMEDIATE);

        wordCardRepository.save(card1);
        wordCardRepository.save(card2);

        List<WordCard> cards = wordCardService.getAllCards();

        assertThat(cards).hasSize(2);
    }

    @Test
    void shouldGetCardById() {
        WordCard card = new WordCard(null, "Cat", "Кот", null, null, WordCard.DifficultyLevel.BEGINNER);
        WordCard saved = wordCardRepository.save(card);

        Optional<WordCard> found = wordCardService.getCardById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEnglishWord()).isEqualTo("Cat");
    }

    @Test
    void shouldSearchCards() {
        WordCard card = new WordCard(null, "Programming", "Программирование", null, null, WordCard.DifficultyLevel.ADVANCED);
        wordCardRepository.save(card);

        List<WordCard> results = wordCardService.searchCards("program");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEnglishWord()).isEqualTo("Programming");
    }

    @Test
    void shouldGetRandomCard() {
        WordCard card = new WordCard(null, "Random", "Случайный", null, null, WordCard.DifficultyLevel.BEGINNER);
        wordCardRepository.save(card);

        WordCard random = wordCardService.getRandomCard();

        assertThat(random).isNotNull();
    }

    @Test
    void shouldDeleteCard() {
        WordCard card = new WordCard(null, "ToDelete", "Удалить", null, null, WordCard.DifficultyLevel.BEGINNER);
        WordCard saved = wordCardRepository.save(card);

        wordCardService.deleteCard(saved.getId());

        Optional<WordCard> deleted = wordCardRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }
}

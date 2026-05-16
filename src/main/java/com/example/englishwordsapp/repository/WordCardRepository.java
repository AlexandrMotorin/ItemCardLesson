package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.WordCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordCardRepository extends JpaRepository<WordCard, Long> {

    List<WordCard> findByDifficultyLevel(WordCard.DifficultyLevel difficultyLevel);

    List<WordCard> findByEnglishWordContainingIgnoreCase(String word);

    @Query(value = "SELECT * FROM word_cards ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<WordCard> findRandomCard();
}

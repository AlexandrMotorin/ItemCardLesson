package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.WordCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordCardRepository extends JpaRepository<WordCard, Long> {

    List<WordCard> findByDifficultyLevel(WordCard.DifficultyLevel difficultyLevel);

    List<WordCard> findByEnglishWordContainingIgnoreCase(String word);
}

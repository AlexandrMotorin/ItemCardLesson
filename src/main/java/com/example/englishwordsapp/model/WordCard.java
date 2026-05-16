package com.example.englishwordsapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "word_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "English word is required")
    @Column(nullable = false)
    private String englishWord;

    @NotBlank(message = "Translation is required")
    @Column(nullable = false)
    private String translation;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    public enum DifficultyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }

    public boolean isSelected(DifficultyLevel lvl) {
        return difficultyLevel != null && difficultyLevel == lvl;
    }
}

package com.example.englishwordsapp.dto;

import com.example.englishwordsapp.model.Direction;
import com.example.englishwordsapp.model.StudyResult;
import com.example.englishwordsapp.model.WordCard;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionDto {
    private final WordCard wordCard;
    private final StudyResult.ExerciseType exerciseType;
    private final Direction direction;
    private final int currentIndex;
    private final int totalWords;
    /** Варианты ответов для MULTIPLE_CHOICE, null для TEXT_INPUT */
    private final List<WordCard> choices;
}

package com.example.englishwordsapp.exception;

public class WordSetAccessDeniedException extends RuntimeException {
    public WordSetAccessDeniedException(String message) {
        super(message);
    }
}
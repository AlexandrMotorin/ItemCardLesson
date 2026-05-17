package com.example.englishwordsapp.exception;

public class WordSetNotFoundException extends RuntimeException {
    public WordSetNotFoundException(Long id) {
        super("WordSet not found with id: " + id);
    }
}
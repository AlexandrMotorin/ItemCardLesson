package com.example.englishwordsapp.exception;

public class SystemSetModificationException extends RuntimeException {
    public SystemSetModificationException() {
        super("Cannot modify a system set");
    }
}
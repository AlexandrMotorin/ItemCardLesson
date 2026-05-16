package com.example.englishwordsapp.util;

public class StringUtil {

    public static String getOrEmpty(String text) {
        return getOrDefault(text, "");
    }

    public static String getOrDefault(String text, String def) {
        return text == null ? def : text;
    }

}

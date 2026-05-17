package com.example.englishwordsapp;

import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.repository.WordSetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WordSetRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private WordSetRepository wordSetRepository;

    @Test
    void shouldFindSystemSets() {
        List<WordSet> systemSets = wordSetRepository.findByIsSystemTrue();

        assertThat(systemSets).isNotEmpty();
        assertThat(systemSets).allMatch(WordSet::isSystem);
    }

    @Test
    void shouldFindByNameContaining() {
        List<WordSet> results = wordSetRepository.findByNameContainingIgnoreCase("verbs");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).containsIgnoringCase("verbs");
    }

    @Test
    void shouldFindAllSets() {
        List<WordSet> allSets = wordSetRepository.findAll();

        assertThat(allSets).isNotEmpty();
        assertThat(allSets)
                .extracting(WordSet::getName)
                .contains("Phrasal Verbs", "Irregular Verbs");
    }

    @Test
    void shouldHaveWordsInSystemSets() {
        List<WordSet> systemSets = wordSetRepository.findByIsSystemTrue();

        for (WordSet set : systemSets) {
            assertThat(set.getWordCards()).isNotEmpty();
        }
    }
}
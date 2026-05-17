package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.WordSetFork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordSetForkRepository extends JpaRepository<WordSetFork, Long> {

    List<WordSetFork> findByUserId(Long userId);

    Optional<WordSetFork> findByOriginalSetIdAndUserId(Long originalSetId, Long userId);

    boolean existsByOriginalSetIdAndUserId(Long originalSetId, Long userId);
}
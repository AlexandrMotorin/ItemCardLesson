package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.WordSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordSetRepository extends JpaRepository<WordSet, Long> {

    List<WordSet> findByOwnerId(Long ownerId);

    List<WordSet> findByIsSystemTrue();

    List<WordSet> findByNameContainingIgnoreCase(String name);

    @Query("SELECT ws FROM WordSet ws WHERE ws.owner.id = :ownerId OR ws.isSystem = true")
    List<WordSet> findByOwnerIdOrIsSystemTrue(@Param("ownerId") Long ownerId);

    Optional<WordSet> findByNameAndOwnerId(String name, Long ownerId);
}
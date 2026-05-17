package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    List<UserWordProgress> findByUserIdAndWordSetId(Long userId, Long wordSetId);

    Optional<UserWordProgress> findByUserIdAndWordCardIdAndWordSetId(Long userId, Long wordCardId, Long wordSetId);

    List<UserWordProgress> findByUserIdAndWordSetIdOrderByWordCard(Long userId, Long wordSetId);
}

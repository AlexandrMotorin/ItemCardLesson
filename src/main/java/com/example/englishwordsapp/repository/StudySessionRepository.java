package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByUserId(Long userId);

    List<StudySession> findByUserIdAndStartedAtAfter(Long userId, LocalDateTime after);
}
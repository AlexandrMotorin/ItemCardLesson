package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.StudyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyResultRepository extends JpaRepository<StudyResult, Long> {

    List<StudyResult> findByStudySessionId(Long studySessionId);

    List<StudyResult> findByStudySession_UserIdAndWordSetId(Long userId, Long wordSetId);

    List<StudyResult> findByStudySession_UserIdAndWordCardIdAndWordSetId(Long userId, Long wordCardId, Long wordSetId);

    long countByStudySession_UserIdAndWordSetId(Long userId, Long wordSetId);

    long countByStudySession_UserIdAndWordSetIdAndIsCorrectTrue(Long userId, Long wordSetId);

    @Query("SELECT COUNT(r), SUM(CASE WHEN r.isCorrect = true THEN 1 ELSE 0 END) " +
           "FROM StudyResult r WHERE r.studySession.user.id = :userId " +
           "AND r.wordCard.id = :wordCardId AND r.wordSet.id = :wordSetId " +
           "AND r.exerciseType = :exerciseType")
    Object[] getExerciseTypeStats(@Param("userId") Long userId,
                                  @Param("wordCardId") Long wordCardId,
                                  @Param("wordSetId") Long wordSetId,
                                  @Param("exerciseType") String exerciseType);
}

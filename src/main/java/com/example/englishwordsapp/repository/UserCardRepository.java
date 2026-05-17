package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.UserCard;
import com.example.englishwordsapp.model.UserCard.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardRepository extends JpaRepository<UserCard, Long> {

    List<UserCard> findByUserId(Long userId);

    Optional<UserCard> findByUserIdAndWordCardId(Long userId, Long cardId);

    boolean existsByUserIdAndWordCardId(Long userId, Long cardId);

    void deleteByUserIdAndWordCardId(Long userId, Long cardId);

    List<UserCard> findByUserIdAndStatus(Long userId, StudyStatus status);

    @Query("SELECT uc.wordCard.id FROM UserCard uc WHERE uc.user.id = :userId")
    List<Long> findCardIdsByUserId(@Param("userId") Long userId);
}

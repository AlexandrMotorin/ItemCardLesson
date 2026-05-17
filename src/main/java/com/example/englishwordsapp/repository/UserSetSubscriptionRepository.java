package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.model.UserSetSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSetSubscriptionRepository extends JpaRepository<UserSetSubscription, Long> {

    List<UserSetSubscription> findByUserId(Long userId);

    Optional<UserSetSubscription> findByUserIdAndWordSetId(Long userId, Long wordSetId);

    boolean existsByUserIdAndWordSetId(Long userId, Long wordSetId);

    void deleteByUserIdAndWordSetId(Long userId, Long wordSetId);
}
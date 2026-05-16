package com.example.englishwordsapp.repository;

import com.example.englishwordsapp.security.AuthProvider;
import com.example.englishwordsapp.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

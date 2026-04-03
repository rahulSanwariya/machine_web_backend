package com.project.machiness_backend.auth.repo;

import com.project.machiness_backend.auth.entity.RefreshToken;
import com.project.machiness_backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    boolean existsByUser(User user);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.isExpired = true WHERE r.user = :user")
    void expireTokenByUser(User user);

    @Transactional
    void deleteByUser(User user);
}
package com.example.olxconnect.repository;

import com.example.olxconnect.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    boolean existsByUsername(String username);

    boolean existsByRefreshToken(String refreshToken);

    Token findByRefreshToken(String refreshToken);

    @Query("SELECT t.accessToken FROM Token t WHERE t.refreshToken = :refreshToken")
    String findAccessTokenByRefreshToken(@Param("refreshToken") String refreshToken);

}

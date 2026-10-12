package com.edumy.repositories;

import com.edumy.entities.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    @Query("select token from RefreshToken token join fetch token.user where token.token = :value")
    Optional<RefreshToken> findByTokenWithUser(@Param("value") String value);
}

package com.edumy.payout.repository;

import com.edumy.payout.model.PayoutAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayoutAccountRepository extends JpaRepository<PayoutAccount, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayoutAccount p WHERE p.instructorId = :instructorId")
    Optional<PayoutAccount> findByIdForUpdate(@Param("instructorId") Integer instructorId);
}

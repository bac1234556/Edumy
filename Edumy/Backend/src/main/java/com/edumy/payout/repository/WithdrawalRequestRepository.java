package com.edumy.payout.repository;

import com.edumy.payout.model.WithdrawalRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Integer> {
    List<WithdrawalRequest> findByInstructorId(Integer instructorId);
    Page<WithdrawalRequest> findByInstructorId(Integer instructorId, Pageable pageable);
    List<WithdrawalRequest> findByInstructorIdAndStatus(Integer instructorId, String status);
    Page<WithdrawalRequest> findByStatus(String status, Pageable pageable);

    @Query("SELECT COUNT(w) FROM WithdrawalRequest w " +
           "WHERE w.instructorId = :instructorId " +
           "AND LOWER(w.status) IN :statuses " +
           "AND w.createdAt >= :startInclusive " +
           "AND w.createdAt < :endExclusive")
    long countActiveInPeriod(@Param("instructorId") Integer instructorId,
                             @Param("statuses") Collection<String> statuses,
                             @Param("startInclusive") LocalDateTime startInclusive,
                             @Param("endExclusive") LocalDateTime endExclusive);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WithdrawalRequest w WHERE w.id = :id")
    Optional<WithdrawalRequest> findByIdForUpdate(@Param("id") Integer id);
}

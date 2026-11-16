package com.edumy.payout.repository;

import com.edumy.payout.model.WithdrawalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Integer> {
    List<WithdrawalRequest> findByInstructorId(Integer instructorId);
    Page<WithdrawalRequest> findByInstructorId(Integer instructorId, Pageable pageable);
    List<WithdrawalRequest> findByInstructorIdAndStatus(Integer instructorId, String status);
    Page<WithdrawalRequest> findByStatus(String status, Pageable pageable);
}

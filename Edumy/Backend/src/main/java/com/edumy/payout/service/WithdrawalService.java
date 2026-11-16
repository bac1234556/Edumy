package com.edumy.payout.service;

import com.edumy.payout.dto.WithdrawalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WithdrawalService {
    WithdrawalResponseDto requestWithdrawal(Integer instructorId, Long amount);
    Page<WithdrawalResponseDto> getMyWithdrawals(Integer instructorId, Pageable pageable);
    void cancelWithdrawal(Integer instructorId, Integer withdrawalId);

    Page<WithdrawalResponseDto> getAdminWithdrawals(String status, Pageable pageable);
    WithdrawalResponseDto payWithdrawal(Integer withdrawalId, String transferReference);
    WithdrawalResponseDto rejectWithdrawal(Integer withdrawalId, String reason);
}

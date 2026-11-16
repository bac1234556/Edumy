package com.edumy.payout.controller;

import com.edumy.payout.dto.WithdrawalRequestDto;
import com.edumy.payout.dto.WithdrawalResponseDto;
import com.edumy.payout.service.WithdrawalService;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @GetMapping
    public ResponseEntity<Page<WithdrawalResponseDto>> getMyWithdrawals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(withdrawalService.getMyWithdrawals(userId, PageRequest.of(page, Math.min(size, 100))));
    }

    @PostMapping
    public ResponseEntity<WithdrawalResponseDto> requestWithdrawal(@Valid @RequestBody WithdrawalRequestDto dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(withdrawalService.requestWithdrawal(userId, dto.getAmount()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelWithdrawal(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        withdrawalService.cancelWithdrawal(userId, id);
        return ResponseEntity.noContent().build();
    }
}

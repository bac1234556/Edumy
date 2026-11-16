package com.edumy.payout.controller;

import com.edumy.payout.dto.WithdrawalResponseDto;
import com.edumy.payout.service.WithdrawalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/withdrawals")
@PreAuthorize("hasRole('Admin')")
public class AdminWithdrawalController {

    private final WithdrawalService withdrawalService;

    public AdminWithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @GetMapping
    public ResponseEntity<Page<WithdrawalResponseDto>> getAdminWithdrawals(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(withdrawalService.getAdminWithdrawals(status, PageRequest.of(page, Math.min(size, 100))));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<WithdrawalResponseDto> payWithdrawal(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        String transferRef = body != null ? body.get("transferReference") : null;
        return ResponseEntity.ok(withdrawalService.payWithdrawal(id, transferRef));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<WithdrawalResponseDto> rejectWithdrawal(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(withdrawalService.rejectWithdrawal(id, reason));
    }
}

package com.edumy.payout.controller;

import com.edumy.payout.dto.PayoutAccountDto;
import com.edumy.payout.dto.UpdatePayoutAccountRequest;
import com.edumy.payout.service.PayoutAccountService;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/payout-account")
public class PayoutAccountController {

    private final PayoutAccountService payoutAccountService;

    public PayoutAccountController(PayoutAccountService payoutAccountService) {
        this.payoutAccountService = payoutAccountService;
    }

    @GetMapping
    public ResponseEntity<PayoutAccountDto> getPayoutAccount() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(payoutAccountService.getAccount(userId));
    }

    @PutMapping
    public ResponseEntity<PayoutAccountDto> updatePayoutAccount(@Valid @RequestBody UpdatePayoutAccountRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(payoutAccountService.updateAccount(userId, request));
    }
}

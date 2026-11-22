package com.edumy.payout.service.impl;

import com.edumy.identity.api.UserPasswordVerificationApi;
import com.edumy.payout.dto.PayoutAccountDto;
import com.edumy.payout.dto.UpdatePayoutAccountRequest;
import com.edumy.payout.model.PayoutAccount;
import com.edumy.payout.repository.PayoutAccountRepository;
import com.edumy.payout.service.PayoutAccountService;
import com.edumy.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class PayoutAccountServiceImpl implements PayoutAccountService {

    private final PayoutAccountRepository payoutAccountRepository;
    private final UserPasswordVerificationApi userPasswordVerificationApi;

    public PayoutAccountServiceImpl(PayoutAccountRepository payoutAccountRepository,
                                    UserPasswordVerificationApi userPasswordVerificationApi) {
        this.payoutAccountRepository = payoutAccountRepository;
        this.userPasswordVerificationApi = userPasswordVerificationApi;
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutAccountDto getAccount(Integer instructorId) {
        PayoutAccount account = payoutAccountRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Chưa khai báo tài khoản nhận tiền."));

        return toDto(account);
    }

    @Override
    @Transactional
    public PayoutAccountDto updateAccount(Integer instructorId, UpdatePayoutAccountRequest request) {
        if (!userPasswordVerificationApi.verifyPassword(instructorId, request.getCurrentPassword())) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CURRENT_PASSWORD_INCORRECT", "Mật khẩu hiện tại không chính xác.");
        }

        PayoutAccount account = payoutAccountRepository.findById(instructorId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (account == null) {
            account = PayoutAccount.builder()
                    .instructorId(instructorId)
                    .bankName(request.getBankName().trim())
                    .accountNumber(request.getAccountNumber().trim())
                    .accountHolder(request.getAccountHolder().trim().toUpperCase())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        } else {
            account.setBankName(request.getBankName().trim());
            account.setAccountNumber(request.getAccountNumber().trim());
            account.setAccountHolder(request.getAccountHolder().trim().toUpperCase());
            account.setUpdatedAt(now);
        }

        PayoutAccount saved = payoutAccountRepository.save(account);
        return toDto(saved);
    }

    private PayoutAccountDto toDto(PayoutAccount account) {
        String num = account.getAccountNumber() != null ? account.getAccountNumber() : "";
        String last4 = num.length() > 4 ? num.substring(num.length() - 4) : num;

        return PayoutAccountDto.builder()
                .bankName(account.getBankName())
                .accountNumberLast4(last4)
                .accountHolder(account.getAccountHolder())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}

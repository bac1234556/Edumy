package com.edumy.payout.service.impl;

import com.edumy.payout.dto.EarningSummaryDto;
import com.edumy.payout.dto.WithdrawalResponseDto;
import com.edumy.payout.model.PayoutAccount;
import com.edumy.payout.model.WithdrawalRequest;
import com.edumy.payout.repository.PayoutAccountRepository;
import com.edumy.payout.repository.WithdrawalRequestRepository;
import com.edumy.payout.service.EarningService;
import com.edumy.payout.service.WithdrawalService;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final PayoutAccountRepository payoutAccountRepository;
    private final EarningService earningService;

    @Value("${edumy.payout.min-withdrawal:100000}")
    private long minWithdrawal;

    public WithdrawalServiceImpl(WithdrawalRequestRepository withdrawalRequestRepository,
                                 PayoutAccountRepository payoutAccountRepository,
                                 EarningService earningService) {
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.payoutAccountRepository = payoutAccountRepository;
        this.earningService = earningService;
    }

    @Override
    @Transactional
    public WithdrawalResponseDto requestWithdrawal(Integer instructorId, Long amount) {
        if (amount == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Vui lòng nhập số tiền hợp lệ.");
        }

        PayoutAccount account = payoutAccountRepository.findByIdForUpdate(instructorId).orElse(null);
        if (account == null) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "PAYOUT_ACCOUNT_REQUIRED",
                    "Vui lòng khai báo tài khoản ngân hàng nhận tiền trước khi rút.");
        }

        if (amount < minWithdrawal) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "WITHDRAWAL_BELOW_MINIMUM",
                    "Số tiền rút tối thiểu là " + minWithdrawal + " đ.");
        }

        List<WithdrawalRequest> openRequests = withdrawalRequestRepository.findByInstructorIdAndStatus(instructorId, "requested");
        if (!openRequests.isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT, "WITHDRAWAL_ALREADY_OPEN",
                    "Bạn đang có một yêu cầu rút tiền đang chờ xử lý.");
        }

        EarningSummaryDto summary = earningService.getSummary(instructorId);
        if (amount > summary.getAvailableBalance()) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "WITHDRAWAL_EXCEEDS_BALANCE",
                    "Số tiền rút (" + amount + " đ) vượt quá số dư khả dụng (" + summary.getAvailableBalance() + " đ).");
        }

        String rawNum = account.getAccountNumber() != null ? account.getAccountNumber() : "";
        String last4 = rawNum.length() > 4 ? rawNum.substring(rawNum.length() - 4) : rawNum;

        WithdrawalRequest request = WithdrawalRequest.builder()
                .instructorId(instructorId)
                .amount(amount)
                .bankName(account.getBankName())
                .accountNumberLast4(last4)
                .accountHolder(account.getAccountHolder())
                .status("requested")
                .createdAt(LocalDateTime.now())
                .build();

        try {
            WithdrawalRequest saved = withdrawalRequestRepository.save(request);
            return toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(HttpStatus.CONFLICT, "WITHDRAWAL_ALREADY_OPEN",
                    "Bạn đang có một yêu cầu rút tiền đang chờ xử lý.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawalResponseDto> getMyWithdrawals(Integer instructorId, Pageable pageable) {
        return withdrawalRequestRepository.findByInstructorId(instructorId, pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public void cancelWithdrawal(Integer instructorId, Integer withdrawalId) {
        WithdrawalRequest req = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy yêu cầu rút tiền."));

        if (!Objects.equals(req.getInstructorId(), instructorId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy yêu cầu rút tiền.");
        }

        if (!"requested".equalsIgnoreCase(req.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "CANNOT_CANCEL",
                    "Chỉ có thể hủy yêu cầu đang ở trạng thái chờ xử lý.");
        }

        req.setStatus("cancelled");
        req.setProcessedAt(LocalDateTime.now());
        withdrawalRequestRepository.save(req);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawalResponseDto> getAdminWithdrawals(String status, Pageable pageable) {
        if (status != null && !status.trim().isEmpty()) {
            return withdrawalRequestRepository.findByStatus(status.trim().toLowerCase(), pageable).map(this::toDto);
        }
        return withdrawalRequestRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public WithdrawalResponseDto payWithdrawal(Integer withdrawalId, String transferReference) {
        if (transferReference == null || transferReference.trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Vui lòng nhập mã giao dịch chuyển khoản.");
        }

        WithdrawalRequest req = withdrawalRequestRepository.findByIdForUpdate(withdrawalId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy yêu cầu rút tiền."));

        if (!"requested".equalsIgnoreCase(req.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "ALREADY_PROCESSED",
                    "Yêu cầu rút tiền này đã được xử lý trước đó.");
        }

        req.setStatus("paid");
        req.setTransferReference(transferReference.trim());
        req.setProcessedAt(LocalDateTime.now());
        WithdrawalRequest saved = withdrawalRequestRepository.save(req);
        return toDto(saved);
    }

    @Override
    @Transactional
    public WithdrawalResponseDto rejectWithdrawal(Integer withdrawalId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Vui lòng nhập lý do từ chối.");
        }

        WithdrawalRequest req = withdrawalRequestRepository.findByIdForUpdate(withdrawalId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy yêu cầu rút tiền."));

        if (!"requested".equalsIgnoreCase(req.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "ALREADY_PROCESSED",
                    "Yêu cầu rút tiền này đã được xử lý trước đó.");
        }

        req.setStatus("rejected");
        req.setRejectReason(reason.trim());
        req.setProcessedAt(LocalDateTime.now());
        WithdrawalRequest saved = withdrawalRequestRepository.save(req);
        return toDto(saved);
    }

    private WithdrawalResponseDto toDto(WithdrawalRequest req) {
        return WithdrawalResponseDto.builder()
                .id(req.getId())
                .amount(req.getAmount())
                .bankName(req.getBankName())
                .accountNumberLast4(req.getAccountNumberLast4())
                .accountHolder(req.getAccountHolder())
                .status(req.getStatus())
                .transferReference(req.getTransferReference())
                .rejectReason(req.getRejectReason())
                .createdAt(req.getCreatedAt())
                .processedAt(req.getProcessedAt())
                .build();
    }
}

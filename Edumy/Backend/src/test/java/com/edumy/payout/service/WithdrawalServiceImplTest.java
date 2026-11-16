package com.edumy.payout.service;

import com.edumy.payout.dto.EarningSummaryDto;
import com.edumy.payout.dto.WithdrawalResponseDto;
import com.edumy.payout.model.PayoutAccount;
import com.edumy.payout.model.WithdrawalRequest;
import com.edumy.payout.repository.PayoutAccountRepository;
import com.edumy.payout.repository.WithdrawalRequestRepository;
import com.edumy.payout.service.impl.WithdrawalServiceImpl;
import com.edumy.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceImplTest {

    @Mock
    private WithdrawalRequestRepository withdrawalRequestRepository;

    @Mock
    private PayoutAccountRepository payoutAccountRepository;

    @Mock
    private EarningService earningService;

    private WithdrawalServiceImpl withdrawalService;

    @BeforeEach
    void setUp() {
        withdrawalService = new WithdrawalServiceImpl(withdrawalRequestRepository, payoutAccountRepository, earningService);
        ReflectionTestUtils.setField(withdrawalService, "minWithdrawal", 100000L);
    }

    @Test
    @DisplayName("Fails if payout account is missing")
    void testMissingPayoutAccount() {
        when(payoutAccountRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 200000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Vui lòng khai báo tài khoản ngân hàng")
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("PAYOUT_ACCOUNT_REQUIRED"));
    }

    @Test
    @DisplayName("Fails if amount is below minimum (100,000 VND)")
    void testBelowMinimum() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 50000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Số tiền rút tối thiểu")
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("WITHDRAWAL_BELOW_MINIMUM"));
    }

    @Test
    @DisplayName("Fails if another request is already open (requested)")
    void testAlreadyOpen() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested"))
                .thenReturn(List.of(WithdrawalRequest.builder().id(10).status("requested").build()));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 200000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đang chờ xử lý")
                .satisfies(ex -> {
                    assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(((BusinessException) ex).getCode()).isEqualTo("WITHDRAWAL_ALREADY_OPEN");
                });
    }

    @Test
    @DisplayName("Fails if amount exceeds available balance")
    void testExceedsBalance() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested")).thenReturn(Collections.emptyList());
        when(earningService.getSummary(1)).thenReturn(EarningSummaryDto.builder().availableBalance(150000L).build());

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 300000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vượt quá số dư khả dụng")
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("WITHDRAWAL_EXCEEDS_BALANCE"));
    }

    @Test
    @DisplayName("Creates withdrawal successfully and masks account number")
    void testSuccess() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested")).thenReturn(Collections.emptyList());
        when(earningService.getSummary(1)).thenReturn(EarningSummaryDto.builder().availableBalance(500000L).build());

        when(withdrawalRequestRepository.save(any())).thenAnswer(invocation -> {
            WithdrawalRequest req = invocation.getArgument(0);
            req.setId(101);
            req.setCreatedAt(LocalDateTime.now());
            return req;
        });

        WithdrawalResponseDto result = withdrawalService.requestWithdrawal(1, 200000L);

        assertThat(result.getId()).isEqualTo(101);
        assertThat(result.getAmount()).isEqualTo(200000L);
        assertThat(result.getAccountNumberLast4()).isEqualTo("5678");
        assertThat(result.getStatus()).isEqualTo("requested");
    }
}

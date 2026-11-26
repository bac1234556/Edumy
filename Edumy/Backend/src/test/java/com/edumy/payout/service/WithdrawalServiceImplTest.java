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
import org.springframework.dao.DataIntegrityViolationException;
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
        ReflectionTestUtils.setField(withdrawalService, "minWithdrawal", 1_000_000L);
        ReflectionTestUtils.setField(withdrawalService, "maxWithdrawalsPerMonth", 1);
        ReflectionTestUtils.setField(withdrawalService, "timeZone", "Asia/Ho_Chi_Minh");
    }

    @Test
    @DisplayName("Fails if payout account is missing")
    void testMissingPayoutAccount() {
        when(payoutAccountRepository.findByIdForUpdate(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 1_200_000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Vui lòng khai báo tài khoản ngân hàng")
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("PAYOUT_ACCOUNT_REQUIRED"));
    }

    @Test
    @DisplayName("Fails if amount is below minimum (1,000,000 VND)")
    void testBelowMinimum() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 999_999L))
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
        when(payoutAccountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested"))
                .thenReturn(List.of(WithdrawalRequest.builder().id(10).status("requested").build()));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 1_200_000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đang chờ xử lý")
                .satisfies(ex -> {
                    assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(((BusinessException) ex).getCode()).isEqualTo("WITHDRAWAL_ALREADY_OPEN");
                });
    }

    @Test
    @DisplayName("Fails with 409 WITHDRAWAL_MONTHLY_LIMIT_REACHED when instructor already has a paid withdrawal in current month")
    void testMonthlyLimitReachedWhenPaidInCurrentMonth() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested")).thenReturn(Collections.emptyList());
        when(withdrawalRequestRepository.countActiveInPeriod(eq(1), any(), any(), any())).thenReturn(1L);

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 1_200_000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Mỗi tháng chỉ được rút tiền 1 lần")
                .satisfies(ex -> {
                    assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(((BusinessException) ex).getCode()).isEqualTo("WITHDRAWAL_MONTHLY_LIMIT_REACHED");
                });

        verify(earningService, never()).getSummary(any());
    }

    @Test
    @DisplayName("Concurrent duplicate request caught by unique index maps to 409 WITHDRAWAL_ALREADY_OPEN")
    void testConcurrentUniqueConstraintMappedTo409() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested")).thenReturn(Collections.emptyList());
        when(withdrawalRequestRepository.countActiveInPeriod(eq(1), any(), any(), any())).thenReturn(0L);
        when(earningService.getSummary(1)).thenReturn(EarningSummaryDto.builder().availableBalance(2_500_000L).build());
        when(withdrawalRequestRepository.save(any())).thenThrow(new DataIntegrityViolationException("UX_WithdrawalRequests_OpenPerInstructor"));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 1_200_000L))
                .isInstanceOf(BusinessException.class)
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
        when(payoutAccountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested")).thenReturn(Collections.emptyList());
        when(withdrawalRequestRepository.countActiveInPeriod(eq(1), any(), any(), any())).thenReturn(0L);
        when(earningService.getSummary(1)).thenReturn(EarningSummaryDto.builder().availableBalance(1_100_000L).build());

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1, 1_500_000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vượt quá số dư khả dụng")
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo("WITHDRAWAL_EXCEEDS_BALANCE"));
    }

    @Test
    @DisplayName("Creates withdrawal successfully when monthly active count is 0 (e.g. only cancelled/rejected in month or paid in previous month)")
    void testSuccess() {
        PayoutAccount account = PayoutAccount.builder()
                .instructorId(1).bankName("BIDV").accountNumber("12345678").accountHolder("NGUYEN VAN A")
                .build();
        when(payoutAccountRepository.findByIdForUpdate(1)).thenReturn(Optional.of(account));
        when(withdrawalRequestRepository.findByInstructorIdAndStatus(1, "requested")).thenReturn(Collections.emptyList());
        when(withdrawalRequestRepository.countActiveInPeriod(eq(1), any(), any(), any())).thenReturn(0L);
        when(earningService.getSummary(1)).thenReturn(EarningSummaryDto.builder().availableBalance(2_500_000L).build());

        when(withdrawalRequestRepository.save(any())).thenAnswer(invocation -> {
            WithdrawalRequest req = invocation.getArgument(0);
            req.setId(101);
            return req;
        });

        WithdrawalResponseDto result = withdrawalService.requestWithdrawal(1, 1_000_000L);

        assertThat(result.getId()).isEqualTo(101);
        assertThat(result.getAmount()).isEqualTo(1_000_000L);
        assertThat(result.getAccountNumberLast4()).isEqualTo("5678");
        assertThat(result.getStatus()).isEqualTo("requested");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> statusesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(withdrawalRequestRepository).countActiveInPeriod(
                eq(1), statusesCaptor.capture(), startCaptor.capture(), endCaptor.capture());
        assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder("requested", "paid");
        assertThat(startCaptor.getValue().getDayOfMonth()).isEqualTo(1);
        assertThat(startCaptor.getValue().toLocalTime()).isEqualTo(java.time.LocalTime.MIDNIGHT);
        assertThat(endCaptor.getValue()).isEqualTo(startCaptor.getValue().plusMonths(1));
    }

    @Test
    @DisplayName("Auto-generates transferReference when admin confirms payout without manual input")
    void testPayWithdrawalAutoGeneratesTransferReference() {
        WithdrawalRequest req = WithdrawalRequest.builder()
                .id(42)
                .instructorId(1)
                .amount(1_000_000L)
                .status("requested")
                .build();
        when(withdrawalRequestRepository.findByIdForUpdate(42)).thenReturn(Optional.of(req));
        when(withdrawalRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalResponseDto result = withdrawalService.payWithdrawal(42, null);

        assertThat(result.getStatus()).isEqualTo("paid");
        assertThat(result.getTransferReference()).isNotBlank();
        assertThat(result.getTransferReference()).startsWith("FT");
        assertThat(result.getTransferReference()).contains("-0042-");
        assertThat(result.getProcessedAt()).isNotNull();
    }
}

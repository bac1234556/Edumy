package com.edumy.payout.service;

import com.edumy.authoring.api.CourseInstructorsApi;
import com.edumy.payout.api.EarningApi;
import com.edumy.payout.dto.EarningSummaryDto;
import com.edumy.payout.model.EarningEntry;
import com.edumy.payout.model.WithdrawalRequest;
import com.edumy.payout.repository.EarningEntryRepository;
import com.edumy.payout.repository.WithdrawalRequestRepository;
import com.edumy.payout.service.impl.EarningServiceImpl;
import com.edumy.shared.time.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EarningServiceImplTest {

    @Mock
    private EarningEntryRepository earningEntryRepository;

    @Mock
    private WithdrawalRequestRepository withdrawalRequestRepository;

    @Mock
    private CourseInstructorsApi courseInstructorsApi;

    private EarningServiceImpl earningService;

    @BeforeEach
    void setUp() {
        earningService = new EarningServiceImpl(earningEntryRepository, withdrawalRequestRepository, courseInstructorsApi);
        ReflectionTestUtils.setField(earningService, "platformFeeBps", 3000);
        ReflectionTestUtils.setField(earningService, "holdDays", 7);
        ReflectionTestUtils.setField(earningService, "minWithdrawal", 1_000_000L);
        ReflectionTestUtils.setField(earningService, "maxWithdrawalsPerMonth", 1);
        ReflectionTestUtils.setField(earningService, "timeZone", "Asia/Ho_Chi_Minh");
    }

    @Test
    @DisplayName("AC-M09: Revenue split for 499,000 VND matches formula exactly")
    void testAcM09RevenueSplit() {
        // Price: 499,000 VND
        // Main instructor: ID 1
        // Co-instructor 1: ID 2, shareBps = 2500 (25%)
        // Co-instructor 2: ID 3, shareBps = 1000 (10%)
        // Platform fee: 3000 bps (30%) -> 149,700 VND
        // Net pool: 349,300 VND
        // Co-1: floor(349,300 * 2500 / 10,000) = 87,325 VND
        // Co-2: floor(349,300 * 1000 / 10,000) = 34,930 VND
        // Main: 349,300 - 87,325 - 34,930 = 227,045 VND

        CourseInstructorsApi.CourseInstructorsSnapshot snapshot = new CourseInstructorsApi.CourseInstructorsSnapshot(
                1,
                List.of(
                        new CourseInstructorsApi.InstructorShare(2, 2500),
                        new CourseInstructorsApi.InstructorShare(3, 1000)
                )
        );
        when(courseInstructorsApi.getInstructorsForCourse(100)).thenReturn(snapshot);
        when(earningEntryRepository.existsByOrderIdAndCourseIdAndInstructorId(any(), any(), any())).thenReturn(false);

        EarningApi.PaidOrderCommand command = new EarningApi.PaidOrderCommand(
                10,
                "ORD-10",
                List.of(new EarningApi.OrderLine(1, 100, "Lập trình React Native", 499000L))
        );

        earningService.recordForPaidOrder(command);

        ArgumentCaptor<EarningEntry> captor = ArgumentCaptor.forClass(EarningEntry.class);
        verify(earningEntryRepository, times(3)).save(captor.capture());

        List<EarningEntry> saved = captor.getAllValues();

        EarningEntry co1 = saved.stream().filter(e -> e.getInstructorId().equals(2)).findFirst().orElseThrow();
        assertThat(co1.getAmount()).isEqualTo(87325L);
        assertThat(co1.getRole()).isEqualTo("co");

        EarningEntry co2 = saved.stream().filter(e -> e.getInstructorId().equals(3)).findFirst().orElseThrow();
        assertThat(co2.getAmount()).isEqualTo(34930L);
        assertThat(co2.getRole()).isEqualTo("co");

        EarningEntry main = saved.stream().filter(e -> e.getInstructorId().equals(1)).findFirst().orElseThrow();
        assertThat(main.getAmount()).isEqualTo(227045L);
        assertThat(main.getRole()).isEqualTo("main");
        assertThat(main.getMaturesAt()).isAfter(main.getEarnedAt().plusDays(6));
    }

    @Test
    @DisplayName("hold-days=0 makes earnings mature immediately")
    void testHoldDaysZeroImmediate() {
        ReflectionTestUtils.setField(earningService, "holdDays", 0);
        when(courseInstructorsApi.getInstructorsForCourse(100))
                .thenReturn(new CourseInstructorsApi.CourseInstructorsSnapshot(1, Collections.emptyList()));
        when(earningEntryRepository.existsByOrderIdAndCourseIdAndInstructorId(any(), any(), any())).thenReturn(false);

        earningService.recordForPaidOrder(new EarningApi.PaidOrderCommand(
                11, "ORD-11", List.of(new EarningApi.OrderLine(2, 100, "Course", 200000L))));

        ArgumentCaptor<EarningEntry> captor = ArgumentCaptor.forClass(EarningEntry.class);
        verify(earningEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getMaturesAt()).isEqualTo(captor.getValue().getEarnedAt());
    }

    @Test
    @DisplayName("Summary computes pending and available balance correctly")
    void testGetSummary() {
        LocalDateTime now = LocalDateTime.now();
        List<EarningEntry> entries = List.of(
                EarningEntry.builder().amount(500000L).maturesAt(now.minusDays(5)).build(), // matured
                EarningEntry.builder().amount(300000L).maturesAt(now.plusDays(10)).build()   // pending
        );
        when(earningEntryRepository.findByInstructorId(1)).thenReturn(entries);
        when(withdrawalRequestRepository.findByInstructorId(1)).thenReturn(Collections.emptyList());

        EarningSummaryDto summary = earningService.getSummary(1);

        assertThat(summary.getTotalEarned()).isEqualTo(800000L);
        assertThat(summary.getPendingBalance()).isEqualTo(300000L);
        assertThat(summary.getAvailableBalance()).isEqualTo(500000L);
        assertThat(summary.getMinWithdrawal()).isEqualTo(1_000_000L);
        assertThat(summary.getHoldDays()).isEqualTo(7);
        assertThat(summary.getNextMaturityAt()).isNotNull();
        assertThat(summary.isHasEarnings()).isTrue();
        assertThat(summary.getMaxWithdrawalsPerMonth()).isEqualTo(1);
        assertThat(summary.getWithdrawalsUsedThisMonth()).isZero();
        assertThat(summary.isCanWithdrawThisMonth()).isTrue();
        assertThat(summary.getNextWithdrawalAt()).isNull();
    }

    @Test
    @DisplayName("Monthly withdrawal quota counts paid/requested in current month and ignores cancelled/rejected/previous month")
    void testGetSummaryMonthlyQuota() {
        LocalDateTime now = TimeUtils.now("Asia/Ho_Chi_Minh");
        LocalDateTime startOfMonth = TimeUtils.startOfCurrentMonth("Asia/Ho_Chi_Minh");

        when(earningEntryRepository.findByInstructorId(1)).thenReturn(List.of(
                EarningEntry.builder().amount(5_000_000L).maturesAt(now.minusDays(20)).build()
        ));

        // 1. Paid in previous month + cancelled/rejected in current month -> still eligible this month
        when(withdrawalRequestRepository.findByInstructorId(1)).thenReturn(List.of(
                WithdrawalRequest.builder().amount(1_000_000L).status("paid").createdAt(startOfMonth.minusDays(1)).build(),
                WithdrawalRequest.builder().amount(1_000_000L).status("cancelled").createdAt(startOfMonth.plusHours(1)).build(),
                WithdrawalRequest.builder().amount(1_000_000L).status("rejected").createdAt(startOfMonth.plusHours(2)).build()
        ));

        EarningSummaryDto eligibleSummary = earningService.getSummary(1);
        assertThat(eligibleSummary.getWithdrawalsUsedThisMonth()).isZero();
        assertThat(eligibleSummary.isCanWithdrawThisMonth()).isTrue();
        assertThat(eligibleSummary.getNextWithdrawalAt()).isNull();
        assertThat(eligibleSummary.getAvailableBalance()).isEqualTo(4_000_000L);

        // 2. Paid in current month -> quota exhausted until next month
        when(withdrawalRequestRepository.findByInstructorId(1)).thenReturn(List.of(
                WithdrawalRequest.builder().amount(1_200_000L).status("paid").createdAt(startOfMonth.plusHours(3)).build()
        ));

        EarningSummaryDto blockedSummary = earningService.getSummary(1);
        assertThat(blockedSummary.getWithdrawalsUsedThisMonth()).isEqualTo(1);
        assertThat(blockedSummary.isCanWithdrawThisMonth()).isFalse();
        assertThat(blockedSummary.getNextWithdrawalAt())
                .isEqualTo(TimeUtils.toInstant(TimeUtils.startOfNextMonth("Asia/Ho_Chi_Minh"), "Asia/Ho_Chi_Minh"));
    }
}

package com.edumy.payout.service.impl;

import com.edumy.authoring.api.CourseInstructorsApi;
import com.edumy.payout.dto.*;
import com.edumy.payout.model.EarningEntry;
import com.edumy.payout.model.WithdrawalRequest;
import com.edumy.payout.repository.EarningEntryRepository;
import com.edumy.payout.repository.WithdrawalRequestRepository;
import com.edumy.payout.service.EarningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EarningServiceImpl implements EarningService {

    private final EarningEntryRepository earningEntryRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final CourseInstructorsApi courseInstructorsApi;

    @Value("${edumy.payout.platform-fee-bps:3000}")
    private int platformFeeBps;

    @Value("${edumy.payout.maturity:monthly}")
    private String maturityPolicy;

    @Value("${edumy.payout.min-withdrawal:100000}")
    private long minWithdrawal;

    public EarningServiceImpl(EarningEntryRepository earningEntryRepository,
                              WithdrawalRequestRepository withdrawalRequestRepository,
                              CourseInstructorsApi courseInstructorsApi) {
        this.earningEntryRepository = earningEntryRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.courseInstructorsApi = courseInstructorsApi;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordForPaidOrder(PaidOrderCommand command) {
        if (command == null || command.lines() == null || command.lines().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maturesAt = "immediate".equalsIgnoreCase(maturityPolicy)
                ? now
                : YearMonth.from(now).plusMonths(1).atDay(1).atStartOfDay();

        for (OrderLine line : command.lines()) {
            long unitPrice = line.unitPrice();
            if (unitPrice <= 0 || line.courseId() == null) {
                continue;
            }

            CourseInstructorsApi.CourseInstructorsSnapshot snapshot =
                    courseInstructorsApi.getInstructorsForCourse(line.courseId());
            if (snapshot == null || snapshot.mainInstructorId() == null) {
                continue;
            }

            long netPool = (unitPrice * (10000 - platformFeeBps)) / 10000;
            long remaining = netPool;
            int totalCoShareBps = 0;

            if (snapshot.coInstructors() != null) {
                for (CourseInstructorsApi.InstructorShare co : snapshot.coInstructors()) {
                    if (co.instructorId() == null || co.shareBps() <= 0) continue;
                    totalCoShareBps += co.shareBps();
                    long coAmount = (netPool * co.shareBps()) / 10000;
                    remaining -= coAmount;

                    if (!earningEntryRepository.existsByOrderIdAndCourseIdAndInstructorId(
                            command.orderId(), line.courseId(), co.instructorId())) {
                        EarningEntry coEntry = EarningEntry.builder()
                                .orderId(command.orderId())
                                .orderCode(command.orderCode())
                                .orderItemId(line.orderItemId())
                                .courseId(line.courseId())
                                .courseTitle(line.courseTitle())
                                .instructorId(co.instructorId())
                                .role("co")
                                .unitPrice(unitPrice)
                                .platformFeeBps(platformFeeBps)
                                .shareBps(co.shareBps())
                                .amount(coAmount)
                                .earnedAt(now)
                                .maturesAt(maturesAt)
                                .build();
                        earningEntryRepository.save(coEntry);
                    }
                }
            }

            // Main instructor
            int mainShareBps = Math.max(0, 10000 - totalCoShareBps);
            long mainAmount = Math.max(0, remaining);

            if (!earningEntryRepository.existsByOrderIdAndCourseIdAndInstructorId(
                    command.orderId(), line.courseId(), snapshot.mainInstructorId())) {
                EarningEntry mainEntry = EarningEntry.builder()
                        .orderId(command.orderId())
                        .orderCode(command.orderCode())
                        .orderItemId(line.orderItemId())
                        .courseId(line.courseId())
                        .courseTitle(line.courseTitle())
                        .instructorId(snapshot.mainInstructorId())
                        .role("main")
                        .unitPrice(unitPrice)
                        .platformFeeBps(platformFeeBps)
                        .shareBps(mainShareBps)
                        .amount(mainAmount)
                        .earnedAt(now)
                        .maturesAt(maturesAt)
                        .build();
                earningEntryRepository.save(mainEntry);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EarningSummaryDto getSummary(Integer instructorId) {
        List<EarningEntry> entries = earningEntryRepository.findByInstructorId(instructorId);
        List<WithdrawalRequest> withdrawals = withdrawalRequestRepository.findByInstructorId(instructorId);

        LocalDateTime now = LocalDateTime.now();
        long totalEarned = 0;
        long pendingBalance = 0;

        for (EarningEntry e : entries) {
            long amt = e.getAmount() != null ? e.getAmount() : 0;
            totalEarned += amt;
            if (e.getMaturesAt() != null && e.getMaturesAt().isAfter(now)) {
                pendingBalance += amt;
            }
        }

        long inWithdrawal = 0;
        long withdrawn = 0;

        for (WithdrawalRequest w : withdrawals) {
            long amt = w.getAmount() != null ? w.getAmount() : 0;
            if ("requested".equalsIgnoreCase(w.getStatus())) {
                inWithdrawal += amt;
            } else if ("paid".equalsIgnoreCase(w.getStatus())) {
                withdrawn += amt;
            }
        }

        long availableBalance = Math.max(0, totalEarned - pendingBalance - inWithdrawal - withdrawn);

        return EarningSummaryDto.builder()
                .hasEarnings(!entries.isEmpty())
                .totalEarned(totalEarned)
                .pendingBalance(pendingBalance)
                .inWithdrawal(inWithdrawal)
                .withdrawn(withdrawn)
                .availableBalance(availableBalance)
                .minWithdrawal(minWithdrawal)
                .maturityPolicy(maturityPolicy)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyEarningDto> getMonthly(Integer instructorId, Integer year) {
        List<EarningEntry> entries = earningEntryRepository.findByInstructorId(instructorId);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter ymFormat = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, List<EarningEntry>> grouped = entries.stream()
                .filter(e -> year == null || (e.getEarnedAt() != null && e.getEarnedAt().getYear() == year))
                .collect(Collectors.groupingBy(e -> e.getEarnedAt() != null ? ymFormat.format(e.getEarnedAt()) : ""));

        List<MonthlyEarningDto> result = new ArrayList<>();
        for (Map.Entry<String, List<EarningEntry>> entry : grouped.entrySet()) {
            if (entry.getKey().isEmpty()) continue;
            long sum = entry.getValue().stream().mapToLong(e -> e.getAmount() != null ? e.getAmount() : 0).sum();
            boolean matured = entry.getValue().stream().allMatch(e -> e.getMaturesAt() == null || !e.getMaturesAt().isAfter(now));
            result.add(new MonthlyEarningDto(entry.getKey(), sum, matured));
        }

        result.sort((a, b) -> b.getMonth().compareTo(a.getMonth()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseEarningDto> getCourses(Integer instructorId) {
        List<EarningEntry> entries = earningEntryRepository.findByInstructorId(instructorId);

        Map<Integer, List<EarningEntry>> byCourse = entries.stream()
                .collect(Collectors.groupingBy(EarningEntry::getCourseId));

        List<CourseEarningDto> result = new ArrayList<>();
        for (Map.Entry<Integer, List<EarningEntry>> entry : byCourse.entrySet()) {
            List<EarningEntry> cEntries = entry.getValue();
            if (cEntries.isEmpty()) continue;
            String courseTitle = cEntries.get(0).getCourseTitle();
            String role = cEntries.get(0).getRole();
            long sum = cEntries.stream().mapToLong(e -> e.getAmount() != null ? e.getAmount() : 0).sum();
            result.add(new CourseEarningDto(entry.getKey(), courseTitle, role, sum));
        }

        result.sort((a, b) -> Long.compare(b.getAmount(), a.getAmount()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EarningEntryDto> getEntries(Integer instructorId, Integer courseId, String month, Pageable pageable) {
        Page<EarningEntry> page = (courseId != null)
                ? earningEntryRepository.findByInstructorIdAndCourseId(instructorId, courseId, pageable)
                : earningEntryRepository.findByInstructorId(instructorId, pageable);

        return page.map(e -> EarningEntryDto.builder()
                .id(e.getId())
                .orderCode(e.getOrderCode())
                .courseId(e.getCourseId())
                .courseTitle(e.getCourseTitle())
                .role(e.getRole())
                .unitPrice(e.getUnitPrice())
                .platformFeeBps(e.getPlatformFeeBps())
                .shareBps(e.getShareBps())
                .amount(e.getAmount())
                .earnedAt(e.getEarnedAt())
                .maturesAt(e.getMaturesAt())
                .build());
    }
}

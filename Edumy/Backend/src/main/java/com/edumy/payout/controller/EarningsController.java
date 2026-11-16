package com.edumy.payout.controller;

import com.edumy.payout.dto.*;
import com.edumy.payout.service.EarningService;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/earnings")
public class EarningsController {

    private final EarningService earningService;

    public EarningsController(EarningService earningService) {
        this.earningService = earningService;
    }

    @GetMapping("/summary")
    public ResponseEntity<EarningSummaryDto> getSummary() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(earningService.getSummary(userId));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyEarningDto>> getMonthly(@RequestParam(required = false) Integer year) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(earningService.getMonthly(userId, year));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseEarningDto>> getCourses() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(earningService.getCourses(userId));
    }

    @GetMapping("/entries")
    public ResponseEntity<Page<EarningEntryDto>> getEntries(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(earningService.getEntries(userId, courseId, month, PageRequest.of(page, Math.min(size, 100))));
    }
}

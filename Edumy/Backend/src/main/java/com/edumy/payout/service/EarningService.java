package com.edumy.payout.service;

import com.edumy.payout.api.EarningApi;
import com.edumy.payout.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EarningService extends EarningApi {
    EarningSummaryDto getSummary(Integer instructorId);
    List<MonthlyEarningDto> getMonthly(Integer instructorId, Integer year);
    List<CourseEarningDto> getCourses(Integer instructorId);
    Page<EarningEntryDto> getEntries(Integer instructorId, Integer courseId, String month, Pageable pageable);
}

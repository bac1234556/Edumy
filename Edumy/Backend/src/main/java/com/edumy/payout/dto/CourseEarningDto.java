package com.edumy.payout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEarningDto {
    private Integer courseId;
    private String courseTitle;
    private String role; // "main" | "co"
    private long amount;
}

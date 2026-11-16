package com.edumy.payout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningEntryDto {
    private Long id;
    private String orderCode;
    private Integer courseId;
    private String courseTitle;
    private String role;
    private Long unitPrice;
    private Integer platformFeeBps;
    private Integer shareBps;
    private Long amount;
    private LocalDateTime earnedAt;
    private LocalDateTime maturesAt;
}

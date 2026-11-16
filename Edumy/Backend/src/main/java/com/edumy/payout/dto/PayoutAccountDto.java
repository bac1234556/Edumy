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
public class PayoutAccountDto {
    private String bankName;
    private String accountNumberLast4;
    private String accountHolder;
    private LocalDateTime updatedAt;
}

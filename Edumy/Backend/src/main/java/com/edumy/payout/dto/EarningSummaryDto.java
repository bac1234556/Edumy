package com.edumy.payout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningSummaryDto {
    private boolean hasEarnings;
    private long totalEarned;
    private long pendingBalance;
    private long inWithdrawal;
    private long withdrawn;
    private long availableBalance;
    private long minWithdrawal;
    private String maturityPolicy;
}

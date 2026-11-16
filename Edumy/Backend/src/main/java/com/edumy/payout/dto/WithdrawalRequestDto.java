package com.edumy.payout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDto {

    @NotNull(message = "Vui lòng nhập số tiền cần rút.")
    private Long amount;
}

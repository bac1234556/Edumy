package com.edumy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateRequestDto {
    private Integer orderId;
    @Builder.Default
    private String paymentMethod = "VNPAY"; // VNPAY, MOMO, STRIPE
    private String returnUrl;
    private String bankCode;
    private String ipAddress;
}

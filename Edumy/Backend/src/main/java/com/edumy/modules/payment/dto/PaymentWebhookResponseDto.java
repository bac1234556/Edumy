package com.edumy.modules.payment.dto;

import com.edumy.modules.order.model.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookResponseDto {
    /**
     * Standard response codes:
     * "00": Confirm Success
     * "01": Order not found
     * "02": Order already confirmed (Idempotent)
     * "97": Invalid Checksum
     * "99": Unknown failure
     */
    private String rspCode;
    private String message;
    private Integer orderId;
    private String orderStatus;
}

package com.edumy.backend.service;

import com.edumy.backend.dto.PaymentCreateRequestDto;
import com.edumy.backend.dto.PaymentUrlResponseDto;
import com.edumy.backend.dto.PaymentWebhookResponseDto;

import java.util.Map;

public interface PaymentService {
    /**
     * Tạo đường dẫn thanh toán qua cổng thanh toán (VNPay / Stripe / MoMo)
     */
    PaymentUrlResponseDto createPaymentUrl(PaymentCreateRequestDto request, Integer currentUserId);

    /**
     * Xử lý webhook server-to-server (IPN) với xác thực HMAC SHA512 checksum.
     * Cập nhật Order -> PAID, tạo Enrollments (ACTIVE), dọn Cart, tăng studentCount.
     */
    PaymentWebhookResponseDto processIpnWebhook(Map<String, String> params);

    /**
     * Xử lý callback client return từ cổng thanh toán sau khi người dùng hoàn tất thanh toán
     */
    PaymentWebhookResponseDto processClientCallback(Map<String, String> params);
}

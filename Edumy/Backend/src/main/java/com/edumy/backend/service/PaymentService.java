package com.edumy.backend.service;

import com.edumy.backend.dto.*;

import java.util.Map;

public interface PaymentService {

    PaymentUrlResponseDto createPaymentUrl(PaymentCreateRequestDto request, Integer currentUserId);

    PaymentWebhookResponseDto processIpnWebhook(Map<String, String> params);

    PaymentWebhookResponseDto processClientCallback(Map<String, String> params);

    VietQrGenerateResponseDto generateVietQrPayment(Integer orderId, Integer currentUserId);

    OrderStatusResponseDto getOrderStatus(Integer orderId);

    PaymentWebhookResponseDto processSepayWebhook(SepayWebhookDto webhookData, String authHeader);
}

package com.edumy.commerce.payment.service;

import com.edumy.commerce.payment.dto.OrderStatusResponseDto;
import com.edumy.commerce.payment.dto.PaymentCreateRequestDto;
import com.edumy.commerce.payment.dto.PaymentUrlResponseDto;
import com.edumy.commerce.payment.dto.PaymentWebhookResponseDto;
import com.edumy.commerce.payment.dto.SepayWebhookDto;
import com.edumy.commerce.payment.dto.VietQrGenerateResponseDto;

import java.util.Map;

public interface PaymentService {

    PaymentUrlResponseDto createPaymentUrl(PaymentCreateRequestDto request, Integer currentUserId);

    PaymentWebhookResponseDto processIpnWebhook(Map<String, String> params);

    PaymentWebhookResponseDto processClientCallback(Map<String, String> params);

    VietQrGenerateResponseDto generateVietQrPayment(Integer orderId, Integer currentUserId);

    OrderStatusResponseDto getOrderStatus(Integer orderId);

    PaymentWebhookResponseDto processSepayWebhook(SepayWebhookDto webhookData, String authHeader);
}

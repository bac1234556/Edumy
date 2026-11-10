package com.edumy.modules.payment.service;

import com.edumy.modules.payment.dto.OrderStatusResponseDto;
import com.edumy.modules.payment.dto.PaymentCreateRequestDto;
import com.edumy.modules.payment.dto.PaymentUrlResponseDto;
import com.edumy.modules.payment.dto.PaymentWebhookResponseDto;
import com.edumy.modules.payment.dto.SepayWebhookDto;
import com.edumy.modules.payment.dto.VietQrGenerateResponseDto;

import java.util.Map;

public interface PaymentService {

    PaymentUrlResponseDto createPaymentUrl(PaymentCreateRequestDto request, Integer currentUserId);

    PaymentWebhookResponseDto processIpnWebhook(Map<String, String> params);

    PaymentWebhookResponseDto processClientCallback(Map<String, String> params);

    VietQrGenerateResponseDto generateVietQrPayment(Integer orderId, Integer currentUserId);

    OrderStatusResponseDto getOrderStatus(Integer orderId);

    PaymentWebhookResponseDto processSepayWebhook(SepayWebhookDto webhookData, String authHeader);
}

package com.edumy.backend.controller;

import com.edumy.backend.dto.PaymentCreateRequestDto;
import com.edumy.backend.dto.PaymentUrlResponseDto;
import com.edumy.backend.dto.PaymentWebhookResponseDto;
import com.edumy.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    private Integer getCurrentUserId() {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            return (name != null && !name.equalsIgnoreCase("anonymousUser")) ? Integer.parseInt(name) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * POST /api/v1/payments/create-url
     * Tạo đường dẫn thanh toán qua cổng thanh toán VNPay / MoMo / Stripe
     */
    @PostMapping({"/api/v1/payments/create-url", "/api/Payment/create-url", "/api/payment/create-url"})
    public ResponseEntity<?> createPaymentUrl(@RequestBody PaymentCreateRequestDto request) {
        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Yêu cầu đăng nhập trước khi thanh toán."));
        }

        try {
            PaymentUrlResponseDto response = paymentService.createPaymentUrl(request, currentUserId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | java.util.NoSuchElementException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi tạo đường dẫn thanh toán: " + ex.getMessage()));
        }
    }

    /**
     * GET /api/v1/payments/vnpay-ipn
     * Webhook server-to-server callback chuẩn từ cổng VNPay (IPN)
     * Xác thực HMAC SHA512 Checksum và phản hồi định dạng chuẩn VNPay (RspCode, Message)
     */
    @GetMapping({"/api/v1/payments/vnpay-ipn", "/api/Payment/ipn", "/api/payment/ipn"})
    public ResponseEntity<?> handleVnPayIpn(@RequestParam Map<String, String> allParams) {
        PaymentWebhookResponseDto result = paymentService.processIpnWebhook(allParams);

        // VNPay IPN bắt buộc trả về JSON { "RspCode": "xx", "Message": "..." }
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", result.getRspCode());
        response.put("Message", result.getMessage());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/payments/vnpay-return
     * Callback trả về trên trình duyệt khi người dùng hoàn tất thanh toán trên VNPay
     */
    @GetMapping({"/api/v1/payments/vnpay-return", "/api/Payment/vnpay-return", "/api/payment/vnpay-return"})
    public ResponseEntity<?> handleVnPayReturn(@RequestParam Map<String, String> allParams) {
        PaymentWebhookResponseDto result = paymentService.processClientCallback(allParams);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/payments/webhook
     * Webhook chung (MoMo, Stripe hoặc hệ thống thanh toán bên thứ ba)
     */
    @PostMapping({"/api/v1/payments/webhook", "/api/Payment/webhook", "/api/payment/webhook"})
    public ResponseEntity<?> handleGenericWebhook(@RequestBody(required = false) Map<String, Object> body,
                                                  @RequestParam(required = false) Map<String, String> params,
                                                  @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {
        Map<String, String> combinedParams = new HashMap<>();
        if (params != null) {
            combinedParams.putAll(params);
        }
        if (body != null) {
            body.forEach((k, v) -> {
                if (v != null) combinedParams.put(k, v.toString());
            });
        }
        if (signature != null) {
            combinedParams.putIfAbsent("vnp_SecureHash", signature);
        }

        PaymentWebhookResponseDto result = paymentService.processIpnWebhook(combinedParams);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/Payment/callback
     * Legacy callback cho client mock hoặc frontend cũ
     */
    @PostMapping({"/api/Payment/callback", "/api/payment/callback", "/api/v1/payments/callback"})
    public ResponseEntity<?> legacyPaymentCallback(@RequestBody Map<String, Object> payload) {
        Map<String, String> params = new HashMap<>();
        payload.forEach((k, v) -> {
            if (v != null) params.put(k, v.toString());
        });
        
        // Mock fallback hash if testing
        if (!params.containsKey("vnp_SecureHash") && params.containsKey("orderId")) {
            params.put("vnp_TxnRef", params.get("orderId"));
            params.put("vnp_ResponseCode", Boolean.TRUE.equals(payload.get("success")) ? "00" : "99");
            params.put("vnp_TransactionStatus", "00");
        }

        PaymentWebhookResponseDto result = paymentService.processIpnWebhook(params);
        return ResponseEntity.ok(result);
    }
}

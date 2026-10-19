package com.edumy.backend.controller;

import com.edumy.backend.dto.*;
import com.edumy.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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
     * POST /api/v1/payments/create-qr
     * GET /api/v1/payments/vietqr/{orderId}
     * Sinh mã QR thanh toán động VietQR LPBank theo chuẩn VietQR API
     */
    @PostMapping({"/api/v1/payments/create-qr", "/api/payments/create-qr", "/api/Payment/create-qr", "/api/payment/create-qr"})
    public ResponseEntity<?> createVietQrPayment(@RequestBody Map<String, Object> body) {
        Integer orderId = null;
        if (body.containsKey("orderId") && body.get("orderId") != null) {
            try {
                orderId = Integer.parseInt(body.get("orderId").toString());
            } catch (NumberFormatException ignored) {}
        }

        if (orderId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu orderId hợp lệ trong yêu cầu."));
        }

        Integer currentUserId = getCurrentUserId();
        try {
            VietQrGenerateResponseDto res = paymentService.generateVietQrPayment(orderId, currentUserId);
            return ResponseEntity.ok(res);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi tạo mã QR VietQR: " + ex.getMessage()));
        }
    }

    @GetMapping({"/api/v1/payments/vietqr/{orderId}", "/api/payments/vietqr/{orderId}", "/api/Payment/vietqr/{orderId}", "/api/payment/vietqr/{orderId}"})
    public ResponseEntity<?> getVietQrByOrderId(@PathVariable Integer orderId) {
        Integer currentUserId = getCurrentUserId();
        try {
            VietQrGenerateResponseDto res = paymentService.generateVietQrPayment(orderId, currentUserId);
            return ResponseEntity.ok(res);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi lấy thông tin VietQR: " + ex.getMessage()));
        }
    }

    /**
     * GET /api/v1/orders/{orderId}/status
     * Polling trạng thái đơn hàng thời gian thực
     */
    @GetMapping({"/api/v1/orders/{orderId}/status", "/api/orders/{orderId}/status", "/api/v1/payments/orders/{orderId}/status"})
    public ResponseEntity<?> getOrderStatus(@PathVariable Integer orderId) {
        try {
            OrderStatusResponseDto status = paymentService.getOrderStatus(orderId);
            return ResponseEntity.ok(status);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi kiểm tra trạng thái đơn hàng: " + ex.getMessage()));
        }
    }


    /**
     * POST /api/v1/payments/webhook/sepay
     * Webhook tự động kích hoạt khóa học khi học viên chuyển khoản VietQR qua SePay / Casso / VietQR
     */
    @PostMapping({
            "/api/v1/payments/webhook/sepay",
            "/api/payments/webhook/sepay",
            "/api/payment/webhook/sepay",
            "/api/v1/payments/webhook/casso",
            "/api/v1/payments/webhook/vietqr",
            "/api/payments/webhook/vietqr"
    })
    public ResponseEntity<?> handleSepayWebhook(@RequestBody(required = false) SepayWebhookDto webhookData,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @RequestHeader(value = "X-Api-Key", required = false) String apiKeyHeader) {
        String effectiveAuth = authHeader != null ? authHeader : apiKeyHeader;
        PaymentWebhookResponseDto result = paymentService.processSepayWebhook(webhookData, effectiveAuth);

        if ("97".equals(result.getRspCode())) {
            return ResponseEntity.status(401).body(result);
        }

        // SePay và Casso quy định phản hồi HTTP 200 { success: true }
        Map<String, Object> response = new HashMap<>();
        response.put("success", "00".equals(result.getRspCode()));
        response.put("message", result.getMessage());
        response.put("orderId", result.getOrderId());
        response.put("status", result.getOrderStatus());
        return ResponseEntity.ok(response);
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
     */
    @GetMapping({"/api/v1/payments/vnpay-ipn", "/api/Payment/ipn", "/api/payment/ipn"})
    public ResponseEntity<?> handleVnPayIpn(@RequestParam Map<String, String> allParams) {
        PaymentWebhookResponseDto result = paymentService.processIpnWebhook(allParams);

        Map<String, String> response = new HashMap<>();
        response.put("RspCode", result.getRspCode());
        response.put("Message", result.getMessage());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/payments/vnpay-return
     */
    @GetMapping({"/api/v1/payments/vnpay-return", "/api/Payment/vnpay-return", "/api/payment/vnpay-return"})
    public ResponseEntity<?> handleVnPayReturn(@RequestParam Map<String, String> allParams) {
        PaymentWebhookResponseDto result = paymentService.processClientCallback(allParams);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/payments/webhook
     * Webhook chung (MoMo, Stripe, v.v.)
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
     */
    @PostMapping({"/api/Payment/callback", "/api/payment/callback", "/api/v1/payments/callback"})
    public ResponseEntity<?> legacyPaymentCallback(@RequestBody Map<String, Object> payload) {
        Map<String, String> params = new HashMap<>();
        payload.forEach((k, v) -> {
            if (v != null) params.put(k, v.toString());
        });
        
        if (!params.containsKey("vnp_SecureHash") && params.containsKey("orderId")) {
            params.put("vnp_TxnRef", params.get("orderId"));
            params.put("vnp_ResponseCode", Boolean.TRUE.equals(payload.get("success")) ? "00" : "99");
            params.put("vnp_TransactionStatus", "00");
        }

        PaymentWebhookResponseDto result = paymentService.processIpnWebhook(params);
        return ResponseEntity.ok(result);
    }
}

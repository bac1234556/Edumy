package com.edumy.commerce.payment.service;

import com.edumy.authoring.repository.CourseRepository;
import com.edumy.commerce.order.model.Order;
import com.edumy.commerce.order.repository.CartItemRepository;
import com.edumy.commerce.order.repository.CartRepository;
import com.edumy.commerce.order.repository.OrderItemRepository;
import com.edumy.commerce.order.repository.OrderRepository;
import com.edumy.commerce.payment.dto.PaymentWebhookResponseDto;
import com.edumy.commerce.payment.dto.SepayWebhookDto;
import com.edumy.commerce.payment.service.impl.PaymentServiceImpl;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.identity.user.service.UserActivityService;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.notification.service.MailService;
import com.edumy.payout.api.EarningApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentIdempotencyQaTest {

    @Mock private OrderRepository orderRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EarningApi earningApi;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private UserActivityService userActivityService;
    @Mock private UserRepository userRepository;
    @Mock private MailService mailService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "webhookApiKey", "test_secret_key_2026");
        ReflectionTestUtils.setField(paymentService, "vnpHashSecret", "test_vnpay_hash_secret_2026");
        ReflectionTestUtils.setField(paymentService, "vietQrBankId", "BIDV");
        ReflectionTestUtils.setField(paymentService, "vietQrBankName", "BIDV");
        ReflectionTestUtils.setField(paymentService, "vietQrAccountNo", "96247TEST");
        ReflectionTestUtils.setField(paymentService, "vietQrAccountName", "EDUMY TEST");
        ReflectionTestUtils.setField(paymentService, "vietQrTemplate", "compact2");
    }

    @Test
    @DisplayName("BR-26: SePay webhook rejects wrong API key with rspCode=97")
    void sepayWebhookRejectsWrongApiKey() {
        SepayWebhookDto dto = new SepayWebhookDto();
        dto.setTransferType("in");
        dto.setTransferAmount(BigDecimal.valueOf(300000));
        dto.setContent("EDUMY101");

        PaymentWebhookResponseDto res = paymentService.processSepayWebhook(dto, "Apikey WRONG_KEY");
        assertEquals("97", res.getRspCode());
        verifyNoInteractions(orderRepository, enrollmentRepository, earningApi);
    }

    @Test
    @DisplayName("DEF-W-001: SePay webhook rejects missing Authorization header with rspCode=97")
    void sepayWebhookRejectsMissingApiKey() {
        SepayWebhookDto dto = new SepayWebhookDto();
        dto.setTransferType("in");
        dto.setTransferAmount(BigDecimal.valueOf(300000));
        dto.setContent("EDUMY101");

        PaymentWebhookResponseDto res = paymentService.processSepayWebhook(dto, null);
        assertEquals("97", res.getRspCode());
        verifyNoInteractions(orderRepository, enrollmentRepository, earningApi);
    }

    @Test
    @DisplayName("DEF-W-002: Late webhook on CANCELLED order flags NEEDS_REVIEW without enrolling")
    void sepayWebhookOnCancelledOrderMarksNeedsReview() {
        Order cancelled = new Order();
        cancelled.setOrderId(303);
        cancelled.setUserId(24);
        cancelled.setTotalAmount(BigDecimal.valueOf(300000));
        cancelled.setStatus("CANCELLED");

        when(orderRepository.findById(303)).thenReturn(Optional.of(cancelled));

        SepayWebhookDto dto = new SepayWebhookDto();
        dto.setTransferType("in");
        dto.setTransferAmount(BigDecimal.valueOf(300000));
        dto.setContent("EDUMY303");

        PaymentWebhookResponseDto res = paymentService.processSepayWebhook(dto, "Apikey test_secret_key_2026");
        assertEquals("05", res.getRspCode());
        assertEquals("NEEDS_REVIEW", cancelled.getStatus());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-27 / NFR-04: Repeated SePay webhook for already PAID order is idempotent (no duplicate enrollment or earning)")
    void sepayWebhookIsIdempotentOnAlreadyPaidOrder() {
        Order paidOrder = new Order();
        paidOrder.setOrderId(101);
        paidOrder.setUserId(24);
        paidOrder.setTotalAmount(BigDecimal.valueOf(300000));
        paidOrder.setStatus("PAID");

        when(orderRepository.findById(101)).thenReturn(Optional.of(paidOrder));

        SepayWebhookDto dto = new SepayWebhookDto();
        dto.setTransferType("in");
        dto.setTransferAmount(BigDecimal.valueOf(300000));
        dto.setContent("EDUMY101");

        PaymentWebhookResponseDto res = paymentService.processSepayWebhook(dto, "Apikey test_secret_key_2026");
        assertEquals("00", res.getRspCode());
        verify(enrollmentRepository, never()).save(any());
        verify(earningApi, never()).recordForPaidOrder(any());
    }

    @Test
    @DisplayName("BR-26: VNPay IPN rejects forged vnp_SecureHash with rspCode=97")
    void vnpayIpnRejectsInvalidChecksum() {
        PaymentWebhookResponseDto res = paymentService.processIpnWebhook(Map.of(
                "vnp_TxnRef", "101",
                "vnp_Amount", "30000000",
                "vnp_ResponseCode", "00",
                "vnp_SecureHash", "FORGED_SIGNATURE"
        ));
        assertEquals("97", res.getRspCode());
        verifyNoInteractions(orderRepository, enrollmentRepository);
    }

    @Test
    @DisplayName("NFR-01 IDOR: Generating VietQR for another student's order throws AccessDeniedException")
    void generateVietQrBlocksCrossUserIdor() {
        Order hv1Order = new Order();
        hv1Order.setOrderId(202);
        hv1Order.setUserId(24);
        hv1Order.setTotalAmount(BigDecimal.valueOf(200000));
        when(orderRepository.findById(202)).thenReturn(Optional.of(hv1Order));

        assertThrows(AccessDeniedException.class,
                () -> paymentService.generateVietQrPayment(202, 25)); // HV2 (25) accessing HV1 (24)
    }
}

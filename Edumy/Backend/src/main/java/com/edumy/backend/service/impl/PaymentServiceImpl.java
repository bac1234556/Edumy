package com.edumy.backend.service.impl;

import com.edumy.backend.dto.PaymentCreateRequestDto;
import com.edumy.backend.dto.PaymentUrlResponseDto;
import com.edumy.backend.dto.PaymentWebhookResponseDto;
import com.edumy.backend.model.Cart;
import com.edumy.backend.model.Course;
import com.edumy.backend.model.Enrollment;
import com.edumy.backend.model.Order;
import com.edumy.backend.model.OrderItem;
import com.edumy.backend.repository.CartItemRepository;
import com.edumy.backend.repository.CartRepository;
import com.edumy.backend.repository.CourseRepository;
import com.edumy.backend.repository.EnrollmentRepository;
import com.edumy.backend.repository.OrderRepository;
import com.edumy.backend.service.PaymentService;
import com.edumy.backend.util.PaymentSecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Value("${payment.vnpay.tmn-code:EDUMY01}")
    private String vnpTmnCode;

    @Value("${payment.vnpay.hash-secret:edumy_vnpay_secret_key_2026_super_secure_checksum_key}")
    private String vnpHashSecret;

    @Value("${payment.vnpay.payment-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPaymentUrl;

    @Value("${payment.vnpay.return-url:http://localhost:5173/payment/vnpay-return}")
    private String defaultReturnUrl;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public PaymentUrlResponseDto createPaymentUrl(PaymentCreateRequestDto request, Integer currentUserId) {
        if (request == null || request.getOrderId() == null) {
            throw new IllegalArgumentException("Mã đơn hàng (orderId) không được để trống");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng ID: " + request.getOrderId()));

        if (currentUserId != null && !currentUserId.equals(order.getUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền thanh toán đơn hàng này");
        }

        if ("PAID".equalsIgnoreCase(order.getStatus()) || "Completed".equalsIgnoreCase(order.getStatus())) {
            return PaymentUrlResponseDto.builder()
                    .orderId(order.getOrderId())
                    .paymentMethod(request.getPaymentMethod())
                    .amount(order.getTotalAmount())
                    .message("Đơn hàng này đã được thanh toán thành công trước đó.")
                    .build();
        }

        // VNPay chuẩn: Số tiền tính bằng VND nhân với 100
        BigDecimal amountVal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        long amountInCents = amountVal.multiply(BigDecimal.valueOf(100)).longValue();

        String txnRef = String.valueOf(order.getOrderId()) + "_" + System.currentTimeMillis();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = formatter.format(LocalDateTime.now());
        String expireDate = formatter.format(LocalDateTime.now().plusMinutes(15));

        String returnUrl = (request.getReturnUrl() != null && !request.getReturnUrl().trim().isEmpty())
                ? request.getReturnUrl()
                : defaultReturnUrl;

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amountInCents));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang Edumy #" + order.getOrderId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", request.getIpAddress() != null ? request.getIpAddress() : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        if (request.getBankCode() != null && !request.getBankCode().trim().isEmpty()) {
            vnpParams.put("vnp_BankCode", request.getBankCode().trim());
        }

        // Tạo chuỗi query đã sắp xếp theo thứ tự bảng chữ cái
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    hashData.append(fieldName).append('=').append(fieldValue);
                    query.append(fieldName).append('=').append(fieldValue);
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Tính toán Checksum HMAC SHA512 theo chuẩn bảo mật VNPay
        String secureHash = PaymentSecurityUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        String fullPaymentUrl = vnpPaymentUrl + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;

        return PaymentUrlResponseDto.builder()
                .orderId(order.getOrderId())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "VNPAY")
                .paymentUrl(fullPaymentUrl)
                .transactionRef(txnRef)
                .amount(order.getTotalAmount())
                .message("Tạo đường dẫn thanh toán bảo mật thành công.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentWebhookResponseDto processIpnWebhook(Map<String, String> params) {
        logger.info("Processing IPN Webhook callback with params: {}", params);

        if (params == null || params.isEmpty()) {
            return PaymentWebhookResponseDto.builder()
                    .rspCode("99")
                    .message("Dữ liệu webhook rỗng")
                    .build();
        }

        // 1. Trích xuất mã băm bảo mật (Checksum / Signature)
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null) {
            secureHash = params.get("signature");
        }

        // 2. Xác thực chữ ký số HMAC SHA512
        boolean isValidSignature = PaymentSecurityUtil.verifyChecksum(params, secureHash, vnpHashSecret);
        if (!isValidSignature) {
            logger.warn("Invalid Checksum verification failed for params: {}", params);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("97")
                    .message("Invalid Checksum (Chữ ký bảo mật không hợp lệ)")
                    .build();
        }

        // 3. Trích xuất mã đơn hàng từ vnp_TxnRef (hỗ trợ dạng '123' hoặc '123_timestamp')
        String txnRef = params.get("vnp_TxnRef");
        Integer orderId = null;
        try {
            if (txnRef != null) {
                orderId = Integer.parseInt(txnRef.split("_")[0]);
            } else if (params.containsKey("orderId")) {
                orderId = Integer.parseInt(params.get("orderId"));
            }
        } catch (Exception ex) {
            logger.error("Could not parse order ID from txnRef: {}", txnRef);
        }

        if (orderId == null) {
            return PaymentWebhookResponseDto.builder()
                    .rspCode("01")
                    .message("Order not found (Không tìm thấy thông tin đơn hàng)")
                    .build();
        }

        // 4. Tìm kiếm đơn hàng trong CSDL
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            logger.warn("Order not found in database with id: {}", orderId);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("01")
                    .orderId(orderId)
                    .message("Order not found (Đơn hàng không tồn tại trong hệ thống)")
                    .build();
        }

        // 5. Kiểm tra tính Idempotent: Nếu đơn hàng đã hoàn tất trước đó thì phản hồi thành công ngay
        if ("PAID".equalsIgnoreCase(order.getStatus()) || "Completed".equalsIgnoreCase(order.getStatus())) {
            logger.info("Order #{} is already marked as PAID/Completed. Returning idempotent code 02.", orderId);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("02")
                    .orderId(orderId)
                    .orderStatus(order.getStatus())
                    .message("Order already confirmed (Đơn hàng đã được xác nhận thanh toán thành công trước đó)")
                    .build();
        }

        // 6. Kiểm tra mã phản hồi kết quả từ cổng thanh toán
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTransactionStatus = params.get("vnp_TransactionStatus");
        boolean isSuccess = "00".equals(vnpResponseCode) && ("00".equals(vnpTransactionStatus) || vnpTransactionStatus == null);

        // Hỗ trợ trường hợp Mock hoặc cổng thanh toán khác
        if (!isSuccess && ("SUCCESS".equalsIgnoreCase(params.get("status")) || "0".equals(params.get("resultCode")))) {
            isSuccess = true;
        }

        if (isSuccess) {
            // Cập nhật trạng thái Order thành PAID
            order.setStatus("PAID");
            orderRepository.save(order);
            Integer orderUserId = order.getUserId();

            // Tự động tạo bản ghi Enrollment (trạng thái ACTIVE) cho các khóa học trong đơn hàng
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                for (OrderItem item : order.getOrderItems()) {
                    Integer courseId = item.getCourseId();

                    boolean exists = enrollmentRepository.existsByUserIdAndCourseId(orderUserId, courseId);
                    if (!exists) {
                        Enrollment enrollment = Enrollment.builder()
                                .userId(orderUserId)
                                .courseId(courseId)
                                .enrolledAt(LocalDateTime.now())
                                .progressPercentage(0)
                                .status("ACTIVE")
                                .build();
                        enrollmentRepository.save(enrollment);
                    } else {
                        // Kích hoạt lại trạng thái ACTIVE nếu học viên đã từng có bản ghi chưa active
                        Enrollment existing = enrollmentRepository.findByUserIdAndCourseId(orderUserId, courseId);
                        if (existing != null && !"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
                            existing.setStatus("ACTIVE");
                            enrollmentRepository.save(existing);
                        }
                    }

                    // Tăng số lượng học viên ghi danh của khóa học
                    Course course = courseRepository.findById(courseId).orElse(null);
                    if (course != null) {
                        int currentCount = course.getStudentCount() != null ? course.getStudentCount() : 0;
                        course.setStudentCount(currentCount + 1);
                        courseRepository.save(course);
                    }
                }
            }

            // Dọn dẹp các món đồ đã mua trong Cart của User
            Cart cart = cartRepository.findByUserId(orderUserId);
            if (cart != null && order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    cartItemRepository.deleteByCartIdAndCourseId(cart.getId(), item.getCourseId());
                }
            }

            logger.info("Order #{} marked as PAID. Courses enrolled and Cart cleaned up for user #{}.", orderId, orderUserId);

            return PaymentWebhookResponseDto.builder()
                    .rspCode("00")
                    .orderId(orderId)
                    .orderStatus("PAID")
                    .message("Confirm Success (Giao dịch thanh toán thành công, đã kích hoạt khóa học)")
                    .build();
        } else {
            // Giao dịch thanh toán thất bại hoặc người dùng hủy giao dịch
            order.setStatus("FAILED");
            orderRepository.save(order);
            logger.warn("Transaction failed for Order #{}, vnp_ResponseCode: {}", orderId, vnpResponseCode);

            return PaymentWebhookResponseDto.builder()
                    .rspCode("00")
                    .orderId(orderId)
                    .orderStatus("FAILED")
                    .message("Payment cancelled or failed at payment gateway")
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentWebhookResponseDto processClientCallback(Map<String, String> params) {
        return processIpnWebhook(params);
    }
}

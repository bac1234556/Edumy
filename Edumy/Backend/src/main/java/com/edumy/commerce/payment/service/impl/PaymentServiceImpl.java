package com.edumy.commerce.payment.service.impl;

import com.edumy.commerce.order.model.Cart;
import com.edumy.commerce.order.repository.CartItemRepository;
import com.edumy.commerce.order.repository.CartRepository;
import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.commerce.order.model.Order;
import com.edumy.commerce.order.model.OrderItem;
import com.edumy.commerce.order.repository.OrderItemRepository;
import com.edumy.commerce.order.repository.OrderRepository;
import com.edumy.commerce.payment.dto.OrderStatusResponseDto;
import com.edumy.commerce.payment.dto.PaymentCreateRequestDto;
import com.edumy.shared.util.PaymentSecurityUtil;
import com.edumy.commerce.payment.service.PaymentService;
import com.edumy.commerce.payment.dto.PaymentUrlResponseDto;
import com.edumy.commerce.payment.dto.PaymentWebhookResponseDto;
import com.edumy.commerce.payment.dto.SepayWebhookDto;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.service.UserActivityService;
import com.edumy.commerce.payment.dto.VietQrGenerateResponseDto;

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
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    // VNPay Configuration
    @Value("${payment.vnpay.tmn-code:EDUMY01}")
    private String vnpTmnCode;

    @Value("${payment.vnpay.hash-secret:edumy_vnpay_secret_key_2026_super_secure_checksum_key}")
    private String vnpHashSecret;

    @Value("${payment.vnpay.payment-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPaymentUrl;

    @Value("${payment.vnpay.return-url:http://localhost:5173/payment/vnpay-return}")
    private String defaultReturnUrl;

    // VietQR / LPBank Configuration
    @Value("${payment.vietqr.bank-id:LPB}")
    private String vietQrBankId;

    @Value("${payment.vietqr.bank-name:LPBank (Ngân hàng Bưu điện Liên Việt)}")
    private String vietQrBankName;

    @Value("${payment.vietqr.account-no:0398201106}")
    private String vietQrAccountNo;

    @Value("${payment.vietqr.account-name:LE VIET BAC}")
    private String vietQrAccountName;

    @Value("${payment.vietqr.template:compact2}")
    private String vietQrTemplate;

    @Value("${payment.webhook.api-key:edumy_sepay_webhook_secret_key_2026}")
    private String webhookApiKey;

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

    @Autowired(required = false)
    private com.edumy.payout.api.EarningApi earningApi;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private com.edumy.identity.user.repository.UserRepository userRepository;

    @Autowired
    private com.edumy.notification.service.MailService mailService;

    // =========================================================================
    // 1. VIETQR GENERATION & POLLING
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public VietQrGenerateResponseDto generateVietQrPayment(Integer orderId, Integer currentUserId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Mã đơn hàng (orderId) không được để trống");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng ID: " + orderId));

        if (currentUserId != null && !currentUserId.equals(order.getUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền truy cập đơn hàng này");
        }

        BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        long amountLong = amount.longValue();

        String transferContent = "EDUMY " + order.getOrderId();
        String encodedContent;
        String encodedAccountName;
        try {
            encodedContent = URLEncoder.encode(transferContent, StandardCharsets.UTF_8.toString());
            encodedAccountName = URLEncoder.encode(vietQrAccountName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            encodedContent = "EDUMY%20" + order.getOrderId();
            encodedAccountName = "LE%20VIET%20BAC";
        }

        // VietQR Dynamic URL: https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-compact2.png?amount=<PRICE>&addInfo=EDUMY%20<ORDER_ID>&accountName=<ACCOUNT_NAME>
        String qrImageUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                vietQrBankId,
                vietQrAccountNo,
                vietQrTemplate,
                amountLong,
                encodedContent,
                encodedAccountName
        );

        Integer firstCourseId = null;
        List<OrderItem> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            items = orderItemRepository.findByOrderId(order.getOrderId());
        }
        if (items != null && !items.isEmpty()) {
            firstCourseId = items.get(0).getCourseId();
        }

        return VietQrGenerateResponseDto.builder()
                .orderId(order.getOrderId())
                .amount(amount)
                .transferContent(transferContent)
                .bankId(vietQrBankId)
                .bankName(vietQrBankName)
                .accountNo(vietQrAccountNo)
                .accountName(vietQrAccountName)
                .qrImageUrl(qrImageUrl)
                .status(order.getStatus())
                .expiresAt(order.getCreatedAt() != null ? order.getCreatedAt().plusMinutes(15) : LocalDateTime.now().plusMinutes(15))
                .firstCourseId(firstCourseId)
                .message("Tạo mã QR VietQR thành công.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponseDto getOrderStatus(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Mã đơn hàng (orderId) không được để trống");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng ID: " + orderId));

        boolean isPaid = "PAID".equalsIgnoreCase(order.getStatus()) || "Completed".equalsIgnoreCase(order.getStatus());

        List<Integer> courseIds = new ArrayList<>();
        Integer firstCourseId = null;
        List<OrderItem> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            items = orderItemRepository.findByOrderId(order.getOrderId());
        }
        if (items != null && !items.isEmpty()) {
            for (OrderItem oi : items) {
                if (oi.getCourseId() != null) {
                    courseIds.add(oi.getCourseId());
                }
            }
            if (!courseIds.isEmpty()) {
                firstCourseId = courseIds.get(0);
            }
        }

        return OrderStatusResponseDto.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .isPaid(isPaid)
                .totalAmount(order.getTotalAmount())
                .courseIds(courseIds)
                .firstCourseId(firstCourseId)
                .message(isPaid ? "Đơn hàng đã được thanh toán thành công." : "Đang chờ học viên quét mã QR thanh toán.")
                .build();
    }

    // =========================================================================
    // 2. SEPAY / CASSO / VIETQR WEBHOOK HANDLER
    // =========================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentWebhookResponseDto processSepayWebhook(SepayWebhookDto webhookData, String authHeader) {
        if (webhookData == null) {
            return PaymentWebhookResponseDto.builder()
                    .rspCode("99")
                    .message("Dữ liệu Webhook rỗng (Empty payload)")
                    .build();
        }

        // 1. BẮT BUỘC xác thực API Key / Bearer Token từ Header (BR-26 / OWASP A07)
        if (webhookApiKey != null && !webhookApiKey.trim().isEmpty()) {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Webhook Unauthorized: Thiếu Header Authorization / X-Api-Key");
                return PaymentWebhookResponseDto.builder()
                        .rspCode("97")
                        .message("Unauthorized: Thiếu API Key xác thực Webhook")
                        .build();
            }
            String cleanAuth = authHeader.replaceFirst("(?i)^Bearer\\s+", "")
                    .replaceFirst("(?i)^Apikey\\s+", "")
                    .trim();
            if (!webhookApiKey.trim().equals(cleanAuth)) {
                logger.warn("Webhook Unauthorized: Sai API Key trong Header");
                return PaymentWebhookResponseDto.builder()
                        .rspCode("97")
                        .message("Unauthorized: Sai API Key hoặc Token xác thực Webhook")
                        .build();
            }
        }

        // 2. Kiểm tra loại giao dịch: bỏ qua tiền ra (transferType == "out")
        if ("out".equalsIgnoreCase(webhookData.getTransferType())) {
            logger.info("--> [SePay Webhook] Bỏ qua giao dịch tiền ra (transferType = out)");
            return PaymentWebhookResponseDto.builder()
                    .rspCode("00")
                    .message("Bỏ qua giao dịch tiền ra (tiền rút)")
                    .build();
        }

        // 3. Trích xuất Order ID và số tiền thực nhận
        Integer orderId = webhookData.getOrderId();
        BigDecimal receivedAmount = webhookData.getTransferAmount() != null
                ? webhookData.getTransferAmount()
                : (webhookData.getAmountIn() != null ? webhookData.getAmountIn() : webhookData.getAmount());

        // Ghép các chuỗi nội dung chuyển khoản để regex tìm kiếm cú pháp "EDUMY <orderId>"
        StringBuilder contentBuilder = new StringBuilder();
        if (webhookData.getTransactionContent() != null) contentBuilder.append(" ").append(webhookData.getTransactionContent());
        if (webhookData.getDescription() != null) contentBuilder.append(" ").append(webhookData.getDescription());
        if (webhookData.getContent() != null) contentBuilder.append(" ").append(webhookData.getContent());
        if (webhookData.getBody() != null) contentBuilder.append(" ").append(webhookData.getBody());
        if (webhookData.getCode() != null) contentBuilder.append(" ").append(webhookData.getCode());

        // Hỗ trợ trường hợp webhook Casso
        if (webhookData.getData() != null && !webhookData.getData().isEmpty()) {
            for (var cassoItem : webhookData.getData()) {
                if (cassoItem.getDescription() != null) {
                    contentBuilder.append(" ").append(cassoItem.getDescription());
                }
                if (receivedAmount == null && cassoItem.getAmount() != null) {
                    receivedAmount = cassoItem.getAmount();
                }
            }
        }

        String fullContent = contentBuilder.toString().trim();

        // Xử lý gói tin kiểm thử từ SePay Webhook Tester (Gửi thử)
        if (fullContent.toUpperCase().contains("TEST") || 
            fullContent.toUpperCase().contains("SEPAY") || 
            (webhookData.getId() != null && webhookData.getId() == 0) ||
            orderId == null && (fullContent.isEmpty() || fullContent.length() < 3)) {
            logger.info("--> [SePay Webhook] Nhận diện và phản hồi thành công gói tin kiểm thử SePay ('Gửi thử'): '{}'", fullContent);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("00")
                    .message("SePay test webhook received successfully")
                    .build();
        }

        if (orderId == null && !fullContent.isEmpty()) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)EDUMY[\\s_-]*([0-9]+)");
            java.util.regex.Matcher matcher = pattern.matcher(fullContent);
            if (matcher.find()) {
                try {
                    orderId = Integer.parseInt(matcher.group(1));
                    logger.info("--> [VietQR Webhook] Nhận diện thành công Order ID #{} từ nội dung: '{}'", orderId, fullContent);
                } catch (Exception ex) {
                    logger.error("Lỗi parse orderId từ regex: ", ex);
                }
            }
        }

        if (orderId == null) {
            logger.warn("--> [VietQR Webhook] Không tìm thấy cú pháp EDUMY <ORDER_ID> trong nội dung: '{}'", fullContent);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("01")
                    .message("Không tìm thấy cú pháp EDUMY <ORDER_ID> trong nội dung chuyển khoản: " + fullContent)
                    .build();
        }

        String gateway = webhookData.getGateway() != null ? webhookData.getGateway() : "VietQR_SePay";
        String ref = webhookData.getReferenceCode() != null
                ? webhookData.getReferenceCode()
                : (webhookData.getReferenceNumber() != null ? webhookData.getReferenceNumber() : String.valueOf(webhookData.getId()));

        return completeOrderAndEnroll(orderId, receivedAmount, gateway, ref);
    }

    // =========================================================================
    // 3. CORE LOGIC: COMPLETE ORDER & AUTO-ENROLL (TRANSACTIONAL)
    // =========================================================================

    @Transactional(rollbackFor = Exception.class)
    public PaymentWebhookResponseDto completeOrderAndEnroll(Integer orderId, BigDecimal receivedAmount, String gatewayName, String transactionRef) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            logger.warn("--> [Payment] Không tìm thấy đơn hàng ID: #{}", orderId);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("01")
                    .orderId(orderId)
                    .message("Order not found (Không tìm thấy đơn hàng #" + orderId + ")")
                    .build();
        }

        // Tính Idempotent: Nếu đơn hàng đã hoàn tất trước đó thì phản hồi thành công ngay lập tức
        if ("PAID".equalsIgnoreCase(order.getStatus()) || "Completed".equalsIgnoreCase(order.getStatus())) {
            logger.info("--> [Payment] Đơn hàng #{} đã ở trạng thái PAID/Completed trước đó. Phản hồi Idempotent thành công.", orderId);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("00")
                    .orderId(orderId)
                    .orderStatus(order.getStatus())
                    .message("Order already confirmed (Đơn hàng đã được xác nhận thanh toán thành công trước đó)")
                    .build();
        }

        // BR-28: Nếu đơn hàng đã bị hủy / hết hạn mà nhận webhook thanh toán muộn -> chuyển chờ đối soát (NEEDS_REVIEW), KHÔNG cấp quyền học
        if ("CANCELLED".equalsIgnoreCase(order.getStatus()) || "EXPIRED".equalsIgnoreCase(order.getStatus())) {
            String prevStatus = order.getStatus();
            order.setStatus("NEEDS_REVIEW");
            order.setReconciliationNote("Nhận thanh toán muộn cho đơn hàng đã " + prevStatus
                    + " (Số tiền: " + receivedAmount + ", Ref: " + transactionRef + ")");
            orderRepository.save(order);
            logger.warn("--> [Payment] Đơn hàng #{} đã {} nhưng nhận thanh toán muộn. Chuyển NEEDS_REVIEW.", orderId, prevStatus);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("05")
                    .orderId(orderId)
                    .orderStatus("NEEDS_REVIEW")
                    .message("Đơn hàng đã " + prevStatus + "; giao dịch đã được đánh dấu chờ đối soát thủ công (NEEDS_REVIEW).")
                    .build();
        }

        if ("NEEDS_REVIEW".equalsIgnoreCase(order.getStatus())
                || "RECONCILE".equalsIgnoreCase(order.getStatus())
                || "REFUNDED_EXTERNAL".equalsIgnoreCase(order.getStatus())) {
            return PaymentWebhookResponseDto.builder()
                    .rspCode("05")
                    .orderId(orderId)
                    .orderStatus(order.getStatus())
                    .message("Đơn hàng đang chờ hoặc đã qua xử lý đối soát (" + order.getStatus() + ").")
                    .build();
        }

        // So khớp số tiền thực nhận với tổng giá trị đơn hàng -> thiếu tiền chuyển NEEDS_REVIEW, không cấp quyền học
        if (receivedAmount != null && order.getTotalAmount() != null) {
            if (receivedAmount.compareTo(order.getTotalAmount()) < 0) {
                order.setStatus("NEEDS_REVIEW");
                order.setReconciliationNote("Số tiền chuyển khoản (" + receivedAmount + ") nhỏ hơn tổng tiền đơn hàng ("
                        + order.getTotalAmount() + ", Ref: " + transactionRef + ")");
                orderRepository.save(order);
                logger.warn("--> [Payment] Đơn hàng #{}: Số tiền nhận ({}) nhỏ hơn tổng tiền đơn hàng ({}). Chuyển NEEDS_REVIEW.",
                        orderId, receivedAmount, order.getTotalAmount());
                return PaymentWebhookResponseDto.builder()
                        .rspCode("04")
                        .orderId(orderId)
                        .orderStatus("NEEDS_REVIEW")
                        .message("Số tiền chuyển khoản (" + receivedAmount + ") chưa đủ tổng tiền đơn hàng (" + order.getTotalAmount() + "). Đơn hàng đã chuyển sang NEEDS_REVIEW.")
                        .build();
            }
        }

        // 1. Cập nhật trạng thái đơn hàng -> PAID
        order.setStatus("PAID");
        orderRepository.save(order);
        Integer orderUserId = order.getUserId();

        List<OrderItem> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            items = orderItemRepository.findByOrderId(order.getOrderId());
        }

        // 2. Tự động ghi danh (Enrollments) với trạng thái ACTIVE và tăng studentCount
        if (items != null && !items.isEmpty()) {
            for (OrderItem item : items) {
                Integer courseId = item.getCourseId();
                if (courseId == null) continue;

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
                    Enrollment existing = enrollmentRepository.findByUserIdAndCourseId(orderUserId, courseId);
                    if (existing != null && !"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
                        existing.setStatus("ACTIVE");
                        enrollmentRepository.save(existing);
                    }
                }

                // Cập nhật số học viên của khóa học
                Course course = courseRepository.findById(courseId).orElse(null);
                if (course != null) {
                    int currentCount = course.getStudentCount() != null ? course.getStudentCount() : 0;
                    course.setStudentCount(currentCount + 1);
                    courseRepository.save(course);
                }
            }

            // Ghi nhận sổ thu nhập giảng viên (EarningApi)
            if (earningApi != null) {
                List<com.edumy.payout.api.EarningApi.OrderLine> earningLines = new ArrayList<>();
                for (OrderItem it : items) {
                    if (it.getCourseId() != null && it.getPrice() != null && it.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                        Course c = courseRepository.findById(it.getCourseId()).orElse(null);
                        String cTitle = c != null ? c.getTitle() : "Khóa học #" + it.getCourseId();
                        earningLines.add(new com.edumy.payout.api.EarningApi.OrderLine(
                                it.getOrderItemId(),
                                it.getCourseId(),
                                cTitle,
                                it.getPrice().longValue()
                        ));
                    }
                }
                if (!earningLines.isEmpty()) {
                    String code = "ORD-" + order.getOrderId();
                    earningApi.recordForPaidOrder(new com.edumy.payout.api.EarningApi.PaidOrderCommand(
                            order.getOrderId(),
                            code,
                            earningLines
                    ));
                }
            }
        }

        // 3. Dọn dẹp DUY NHẤT các món hàng vừa được thanh toán thành công trong Cart của học viên (nếu có)
        Cart cart = cartRepository.findByUserId(orderUserId);
        if (cart != null && items != null) {
            Set<Integer> paidCourseIds = items.stream()
                    .map(OrderItem::getCourseId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (Integer pCourseId : paidCourseIds) {
                cartItemRepository.deleteByCartIdAndCourseId(cart.getId(), pCourseId);
            }
            if (cart.getCartItems() != null) {
                cart.getCartItems().removeIf(ci -> paidCourseIds.contains(ci.getCourseId()));
                cart.setUpdatedAt(LocalDateTime.now());
                cartRepository.save(cart);
            }
        }

        // 4. Ghi nhận log kiểm toán hoạt động người dùng
        if (userActivityService != null && orderUserId != null) {
            userActivityService.logActivity(orderUserId, "PAYMENT_COMPLETED", 
                    "Order #" + orderId + " - Total: " + order.getTotalAmount() + " VND via " + (gatewayName != null ? gatewayName : "VietQR"));
        }

        logger.info("--> [Payment] Đơn hàng #{} đã thanh toán thành công qua {}. Đã kích hoạt toàn bộ khóa học cho User #{}.", 
                orderId, gatewayName, orderUserId);

        // 5. Gửi email xác nhận thanh toán đơn hàng qua Mailpit
        if (mailService != null && userRepository != null && orderUserId != null) {
            User buyer = userRepository.findById(orderUserId).orElse(null);
            if (buyer != null && buyer.getEmail() != null) {
                List<String> titles = new ArrayList<>();
                if (items != null) {
                    for (OrderItem item : items) {
                        if (item.getCourseId() != null) {
                            courseRepository.findById(item.getCourseId()).ifPresent(c -> titles.add(c.getTitle()));
                        }
                    }
                }
                long totalPaid = order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0L;
                mailService.sendPurchaseConfirmed(buyer.getEmail(), buyer.getFullName(),
                        "EDUMY" + orderId, totalPaid, titles);
            }
        }

        return PaymentWebhookResponseDto.builder()
                .rspCode("00")
                .orderId(orderId)
                .orderStatus("PAID")
                .message("Xác nhận thanh toán thành công! Khóa học đã được mở khóa tự động.")
                .build();
    }

    // =========================================================================
    // 4. LEGACY VNPAY PAYMENT GATEWAY
    // =========================================================================

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

        String secureHash = PaymentSecurityUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        String fullPaymentUrl = vnpPaymentUrl + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;

        return PaymentUrlResponseDto.builder()
                .orderId(order.getOrderId())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "VNPAY")
                .paymentUrl(fullPaymentUrl)
                .amount(order.getTotalAmount())
                .message("Khởi tạo đường dẫn thanh toán VNPay thành công.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentWebhookResponseDto processIpnWebhook(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return PaymentWebhookResponseDto.builder()
                    .rspCode("99")
                    .message("Dữ liệu callback rỗng")
                    .build();
        }

        // BẮT BUỘC kiểm tra chữ ký bảo mật VNPay / callback (chống giả mạo OWASP A08)
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || secureHash.trim().isEmpty()) {
            logger.warn("Missing secure hash signature in payment IPN/callback");
            return PaymentWebhookResponseDto.builder()
                    .rspCode("97")
                    .message("Missing Checksum (Thiếu chữ ký điện tử xác thực giao dịch)")
                    .build();
        }
        boolean isValidSignature = PaymentSecurityUtil.verifyChecksum(params, secureHash, vnpHashSecret);
        if (!isValidSignature) {
            logger.warn("Invalid VNPay secure hash signature received in IPN");
            return PaymentWebhookResponseDto.builder()
                    .rspCode("97")
                    .message("Invalid Checksum (Chữ ký điện tử không hợp lệ)")
                    .build();
        }

        Integer orderId = null;
        String txnRef = params.get("vnp_TxnRef");
        if (txnRef != null && !txnRef.isEmpty()) {
            String[] parts = txnRef.split("_");
            try {
                orderId = Integer.parseInt(parts[0]);
            } catch (NumberFormatException ignored) {}
        }

        if (orderId == null && params.containsKey("orderId")) {
            try {
                orderId = Integer.parseInt(params.get("orderId"));
            } catch (NumberFormatException ignored) {}
        }

        if (orderId == null) {
            return PaymentWebhookResponseDto.builder()
                    .rspCode("01")
                    .message("Order not found (Không tìm thấy thông tin đơn hàng)")
                    .build();
        }

        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTransactionStatus = params.get("vnp_TransactionStatus");
        boolean isSuccess = "00".equals(vnpResponseCode) && ("00".equals(vnpTransactionStatus) || vnpTransactionStatus == null);

        if (!isSuccess && ("SUCCESS".equalsIgnoreCase(params.get("status")) || "0".equals(params.get("resultCode")))) {
            isSuccess = true;
        }

        if (isSuccess) {
            return completeOrderAndEnroll(orderId, null, "VNPAY", txnRef);
        } else {
            logger.warn("Payment failed for Order #{}, code: {}, status: {}", orderId, vnpResponseCode, vnpTransactionStatus);
            return PaymentWebhookResponseDto.builder()
                    .rspCode("00")
                    .orderId(orderId)
                    .orderStatus("Failed")
                    .message("Payment cancelled or failed at payment gateway")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentWebhookResponseDto processClientCallback(Map<String, String> params) {
        return processIpnWebhook(params);
    }
}

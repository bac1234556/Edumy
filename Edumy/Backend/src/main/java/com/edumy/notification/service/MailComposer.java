package com.edumy.notification.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.edumy.notification.integration.MailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * Builds the Vietnamese emails from templates/mail/<name>.html.
 */
@Service
public class MailComposer {

    public static final String VERIFY_EMAIL = "verify-email";
    public static final String PASSWORD_RESET = "password-reset";
    public static final String PASSWORD_CHANGED = "password-changed";
    public static final String CO_INSTRUCTOR_INVITED = "co-instructor-invited";
    public static final String INVITATIONS_PATH = "/instructor/invitations";
    public static final String COURSE_APPROVED = "course-approved";
    public static final String COURSE_REJECTED = "course-rejected";
    public static final String COURSE_HIDDEN = "course-hidden";
    public static final String QUESTION_ANSWERED = "question-answered";
    public static final String PURCHASE_CONFIRMED = "purchase-confirmed";
    public static final String PAYMENT_RECONCILED = "payment-reconciled";
    public static final String ORDER_PATH = "/orders/";
    public static final String PUBLIC_COURSE_PATH = "/courses/";
    public static final String INSTRUCTOR_COURSE_PATH = "/instructor/courses/";

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z][A-Za-z0-9]*)}");
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter CHANGED_AT =
            DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy '(giờ Việt Nam)'").withZone(VIETNAM);

    private final String baseUrl;
    private final Map<String, String> templates;

    public MailComposer(@Value("${edumy.frontend.base-url:http://localhost:3000}") String baseUrl) {
        this.baseUrl = stripTrailingSlashes(baseUrl);
        this.templates = Map.of(
                VERIFY_EMAIL, load(VERIFY_EMAIL),
                PASSWORD_RESET, load(PASSWORD_RESET),
                PASSWORD_CHANGED, load(PASSWORD_CHANGED),
                CO_INSTRUCTOR_INVITED, load(CO_INSTRUCTOR_INVITED),
                COURSE_APPROVED, load(COURSE_APPROVED),
                COURSE_REJECTED, load(COURSE_REJECTED),
                COURSE_HIDDEN, load(COURSE_HIDDEN),
                QUESTION_ANSWERED, load(QUESTION_ANSWERED),
                PURCHASE_CONFIRMED, load(PURCHASE_CONFIRMED),
                PAYMENT_RECONCILED, load(PAYMENT_RECONCILED));
    }

    /** Verification link. */
    public MailMessage emailVerification(String to, String fullName, String rawCode) {
        return compose(VERIFY_EMAIL, to, "Xác thực địa chỉ email tài khoản Edumy", Map.of(
                "fullName", fullName,
                "verifyUrl", link("/verify-email", rawCode)));
    }

    /** Password reset link. */
    public MailMessage passwordReset(String to, String fullName, String rawCode) {
        return compose(PASSWORD_RESET, to, "Yêu cầu đặt lại mật khẩu tài khoản Edumy", Map.of(
                "fullName", fullName,
                "resetUrl", link("/reset-password", rawCode)));
    }

    /** Password changed notice. */
    public MailMessage passwordChanged(String to, String fullName, Instant changedAt) {
        return compose(PASSWORD_CHANGED, to, "Mật khẩu tài khoản Edumy đã được thay đổi", Map.of(
                "fullName", fullName,
                "changedAt", CHANGED_AT.format(Objects.requireNonNull(changedAt, "changedAt"))));
    }

    /** Co-instructor invitation. */
    public MailMessage coInstructorInvited(String to, String fullName, String inviterName, String courseTitle) {
        return compose(CO_INSTRUCTOR_INVITED, to,
                "Lời mời làm đồng giảng viên khóa học “" + courseTitle + "” trên Edumy", Map.of(
                        "fullName", fullName,
                        "inviterName", inviterName,
                        "courseTitle", courseTitle,
                        "invitationsUrl", baseUrl + INVITATIONS_PATH));
    }

    /** Question answered notification. */
    public MailMessage questionAnswered(String to, String fullName, String instructorName, String questionTitle,
                                        String courseTitle, String slug, Long questionId) {
        return compose(QUESTION_ANSWERED, to,
                "Giảng viên đã trả lời câu hỏi của bạn trong khóa học “" + courseTitle + "”", Map.of(
                        "fullName", fullName,
                        "instructorName", instructorName,
                        "questionTitle", questionTitle,
                        "courseTitle", courseTitle,
                        "questionUrl", baseUrl + PUBLIC_COURSE_PATH + slug + "/learn?question=" + questionId));
    }

    /** Course approved notice. */
    public MailMessage courseApproved(String to, String fullName, String courseTitle, String slug) {
        return compose(COURSE_APPROVED, to, "Khóa học “" + courseTitle + "” của bạn đã được duyệt và phát hành",
                Map.of(
                        "fullName", fullName,
                        "courseTitle", courseTitle,
                        "courseUrl", baseUrl + PUBLIC_COURSE_PATH + slug));
    }

    /** Course rejected notice. */
    public MailMessage courseRejected(String to, String fullName, String courseTitle, Long courseId, String reason) {
        return compose(COURSE_REJECTED, to, "Khóa học “" + courseTitle + "” cần chỉnh sửa trước khi phát hành",
                Map.of(
                        "fullName", fullName,
                        "courseTitle", courseTitle,
                        "reason", reason,
                        "courseUrl", instructorCourseUrl(courseId)));
    }

    /** Course hidden notice. */
    public MailMessage courseHidden(String to, String fullName, String courseTitle, Long courseId, String reason) {
        return compose(COURSE_HIDDEN, to, "Khóa học “" + courseTitle + "” đã tạm thời bị ẩn khỏi hệ thống", Map.of(
                "fullName", fullName,
                "courseTitle", courseTitle,
                "reason", reason,
                "courseUrl", instructorCourseUrl(courseId)));
    }

    /** Purchase confirmation. */
    public MailMessage purchaseConfirmed(String to, String fullName, String orderCode, long totalAmount,
                                         List<String> courseTitles) {
        return compose(PURCHASE_CONFIRMED, to, "Xác nhận đơn hàng " + orderCode + " thành công trên Edumy", Map.of(
                "fullName", fullName,
                "orderCode", orderCode,
                "totalAmount", vnd(totalAmount),
                "courseTitles", courseTitles.isEmpty() ? "Các khóa học đã mua" : quoted(courseTitles),
                "orderUrl", orderUrl(orderCode)));
    }

    /** Payment reconciliation result. */
    public MailMessage paymentReconciled(String to, String fullName, String orderCode, boolean activated,
                                         List<String> courseTitles) {
        String title = activated ? "Thanh toán của bạn đã được xác nhận" : "Giao dịch của bạn đã được hoàn tiền";
        String text = activated
                ? (courseTitles.isEmpty() ? "Thanh toán đã được ghi nhận; bạn đã sở hữu các khóa học của đơn này."
                : "Thanh toán đã được ghi nhận và các khóa học sau đã được thêm vào danh sách của bạn: "
                + quoted(courseTitles) + ".")
                : "Khoản thanh toán được hoàn trả cho bạn ngoài hệ thống Edumy; đơn hàng không cấp thêm quyền học.";
        return compose(PAYMENT_RECONCILED, to, "Kết quả đối soát đơn hàng " + orderCode + " trên Edumy", Map.of(
                "fullName", fullName,
                "orderCode", orderCode,
                "resultTitle", title,
                "resultText", text,
                "orderUrl", orderUrl(orderCode)));
    }

    private String orderUrl(String orderCode) {
        return baseUrl + ORDER_PATH + Objects.requireNonNull(orderCode, "orderCode");
    }

    static String vnd(long amount) {
        return String.format(java.util.Locale.ROOT, "%,d", amount).replace(',', '.') + " đ";
    }

    private static String quoted(List<String> titles) {
        return titles.stream().map(title -> "“" + title + "”").collect(java.util.stream.Collectors.joining(", "));
    }

    private String instructorCourseUrl(Long courseId) {
        return baseUrl + INSTRUCTOR_COURSE_PATH + Objects.requireNonNull(courseId, "courseId");
    }

    private MailMessage compose(String template, String to, String subject, Map<String, String> values) {
        return new MailMessage(template, to, subject, fill(template, templates.get(template), values));
    }

    private String link(String path, String rawCode) {
        return baseUrl + path + "?token=" + URLEncoder.encode(Objects.requireNonNull(rawCode, "rawCode"),
                StandardCharsets.UTF_8);
    }

    static String fill(String templateName, String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder(template.length() + 256);
        Set<String> used = new HashSet<>();
        int placeholders = 0;
        while (matcher.find()) {
            placeholders++;
            String name = matcher.group(1);
            String value = values.get(name);
            if (value == null) {
                throw new IllegalStateException(
                        "Mail template " + templateName + ": no value for placeholder ${" + name + "}");
            }
            used.add(name);
            matcher.appendReplacement(out, Matcher.quoteReplacement(HtmlUtils.htmlEscape(value, "UTF-8")));
        }
        matcher.appendTail(out);
        if (countOccurrences(template, "${") != placeholders) {
            throw new IllegalStateException("Mail template " + templateName + ": malformed ${...} placeholder");
        }
        Set<String> unused = new TreeSet<>(values.keySet());
        unused.removeAll(used);
        if (!unused.isEmpty()) {
            throw new IllegalStateException("Mail template " + templateName + ": no placeholder for " + unused);
        }
        return out.toString();
    }

    private static int countOccurrences(String text, String part) {
        int count = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + part.length())) {
            count++;
        }
        return count;
    }

    private static String load(String name) {
        ClassPathResource resource = new ClassPathResource("templates/mail/" + name + ".html");
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Mail template missing: templates/mail/" + name + ".html", e);
        }
    }

    private static String stripTrailingSlashes(String url) {
        String value = Objects.requireNonNull(url, "edumy.frontend.base-url").strip();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.isEmpty()) {
            throw new IllegalStateException("edumy.frontend.base-url must not be blank");
        }
        return value;
    }
}

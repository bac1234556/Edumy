package com.edumy.learning.enrollment.controller;

import com.edumy.shared.security.SecurityUtils;
import com.edumy.learning.integration.CertificatePdfData;
import com.edumy.learning.integration.CertificatePdfRenderer;
import com.edumy.authoring.model.Course;
import com.edumy.learning.enrollment.model.Certificate;
import com.edumy.learning.enrollment.repository.CertificateRepository;
import com.edumy.identity.user.model.User;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping({"/api/v1/Certificates", "/api/v1/certificates", "/api/Certificates", "/api/certificates"})
public class CertificatesController {

    private static final int VERIFY_RATE_LIMIT_PER_MIN = 30;
    private final Map<String, Deque<Instant>> verifyRateBuckets = new ConcurrentHashMap<>();

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificatePdfRenderer certificatePdfRenderer;

    @Value("${edumy.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    /**
     * Tra cứu thông tin chứng chỉ bằng mã certificateUrl / code (JSON)
     */
    @GetMapping("/{url}")
    public ResponseEntity<?> getCertificateByUrl(@PathVariable String url) {
        Certificate certificate = certificateRepository.findByCertificateUrl(url);

        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = buildCertificateDetail(certificate);
        return ResponseEntity.ok(response);
    }

    /**
     * Tải hoặc xem trực tiếp file PDF chứng chỉ (A4 ngang, font tiếng Việt OpenPDF)
     */
    @GetMapping("/{url}/pdf")
    public ResponseEntity<byte[]> getCertificatePdf(@PathVariable String url) {
        Certificate certificate = certificateRepository.findByCertificateUrl(url);
        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = renderPdfForCertificate(certificate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"certificate-" + url + ".pdf\"")
                .body(pdfBytes);
    }

    /**
     * Tải trực tiếp PDF chứng chỉ của khóa học mà học viên hiện tại đã hoàn thành
     */
    @GetMapping("/my/{courseId}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getMyCourseCertificatePdf(@PathVariable Integer courseId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Certificate certificate = certificateRepository.findByUserIdAndCourseId(userId, courseId);
        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = renderPdfForCertificate(certificate);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"certificate-" + certificate.getCertificateUrl() + ".pdf\"")
                .body(pdfBytes);
    }

    /**
     * API công khai xác thực tính thật giả của chứng chỉ (Verification endpoint)
     * Giới hạn 30 yêu cầu/phút mỗi IP (NFR-03), mã không tồn tại trả 404 (AC-36).
     */
    @GetMapping("/verify/{code}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String code, HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        if (isVerifyRateLimited(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, "60")
                    .body(Map.of(
                            "valid", false,
                            "code", "RATE_LIMIT_EXCEEDED",
                            "message", "Quá nhiều yêu cầu xác minh chứng chỉ. Vui lòng thử lại sau 60 giây."
                    ));
        }

        Certificate certificate = certificateRepository.findByCertificateUrl(code);
        if (certificate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "valid", false,
                    "message", "Mã chứng chỉ không tồn tại trong hệ thống Edumy."
            ));
        }

        Map<String, Object> detail = buildCertificateDetail(certificate);
        detail.put("valid", true);
        detail.put("message", "Chứng chỉ hợp lệ và đã được cấp bởi nền tảng Edumy.");
        return ResponseEntity.ok(detail);
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private boolean isVerifyRateLimited(String ip) {
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(60);
        Deque<Instant> timestamps = verifyRateBuckets.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= VERIFY_RATE_LIMIT_PER_MIN) {
                return true;
            }
            timestamps.addLast(now);
            return false;
        }
    }

    /**
     * Danh sách tất cả chứng chỉ của học viên đang đăng nhập
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyCertificates() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<Certificate> list = certificateRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Certificate c : list) {
            result.add(buildCertificateDetail(c));
        }

        return ResponseEntity.ok(result);
    }

    private byte[] renderPdfForCertificate(Certificate certificate) {
        String studentName = certificate.getStudentNameSnapshot() != null
                ? certificate.getStudentNameSnapshot()
                : ((certificate.getUser() != null && certificate.getUser().getFullName() != null)
                        ? certificate.getUser().getFullName()
                        : "Học viên Edumy");

        String courseTitle = certificate.getCourseTitleSnapshot() != null
                ? certificate.getCourseTitleSnapshot()
                : ((certificate.getCourse() != null && certificate.getCourse().getTitle() != null)
                        ? certificate.getCourse().getTitle()
                        : "Khóa học trực tuyến");

        String instructorName = certificate.getInstructorNameSnapshot() != null
                ? certificate.getInstructorNameSnapshot()
                : ((certificate.getCourse() != null && certificate.getCourse().getInstructor() != null)
                        ? certificate.getCourse().getInstructor().getFullName()
                        : "Ban Giảng huấn Edumy");

        String code = certificate.getCertificateUrl();
        String base = frontendBaseUrl != null ? frontendBaseUrl.replaceAll("/+$", "") : "http://localhost:3000";
        String verifyUrl = base + "/certificates/verify/" + code;

        CertificatePdfData pdfData = new CertificatePdfData(
                code,
                studentName,
                courseTitle,
                instructorName,
                certificate.getIssuedAt() != null ? certificate.getIssuedAt() : java.time.LocalDateTime.now(),
                verifyUrl
        );

        return certificatePdfRenderer.render(pdfData);
    }

    private Map<String, Object> buildCertificateDetail(Certificate certificate) {
        Map<String, Object> response = new HashMap<>();
        String studentName = certificate.getStudentNameSnapshot() != null
                ? certificate.getStudentNameSnapshot()
                : (certificate.getUser() != null ? certificate.getUser().getFullName() : null);
        String courseName = certificate.getCourseTitleSnapshot() != null
                ? certificate.getCourseTitleSnapshot()
                : (certificate.getCourse() != null ? certificate.getCourse().getTitle() : null);
        String instructorName = certificate.getInstructorNameSnapshot() != null
                ? certificate.getInstructorNameSnapshot()
                : ((certificate.getCourse() != null && certificate.getCourse().getInstructor() != null)
                        ? certificate.getCourse().getInstructor().getFullName()
                        : null);
        response.put("certificateId", certificate.getId());
        response.put("issuedAt", certificate.getIssuedAt());
        response.put("studentName", studentName);
        response.put("courseName", courseName);
        response.put("courseTitle", courseName);
        response.put("courseId", certificate.getCourseId());
        response.put("instructorName", instructorName);
        response.put("certificateUrl", certificate.getCertificateUrl());
        response.put("pdfDownloadUrl", "/api/certificates/" + certificate.getCertificateUrl() + "/pdf");
        return response;
    }
}

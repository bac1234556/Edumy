package com.edumy.learning.enrollment.controller;

import com.edumy.shared.security.SecurityUtils;
import com.edumy.learning.integration.CertificatePdfData;
import com.edumy.learning.integration.CertificatePdfRenderer;
import com.edumy.authoring.model.Course;
import com.edumy.learning.enrollment.model.Certificate;
import com.edumy.learning.enrollment.repository.CertificateRepository;
import com.edumy.identity.user.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping({"/api/v1/Certificates", "/api/v1/certificates", "/api/Certificates", "/api/certificates"})
public class CertificatesController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificatePdfRenderer certificatePdfRenderer;

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
     */
    @GetMapping("/verify/{code}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String code) {
        Certificate certificate = certificateRepository.findByCertificateUrl(code);
        if (certificate == null) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Mã chứng chỉ không tồn tại trong hệ thống Edumy."
            ));
        }

        Map<String, Object> detail = buildCertificateDetail(certificate);
        detail.put("valid", true);
        detail.put("message", "Chứng chỉ hợp lệ và đã được cấp bởi nền tảng Edumy.");
        return ResponseEntity.ok(detail);
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
        String studentName = (certificate.getUser() != null && certificate.getUser().getFullName() != null)
                ? certificate.getUser().getFullName()
                : "Học viên Edumy";

        String courseTitle = (certificate.getCourse() != null && certificate.getCourse().getTitle() != null)
                ? certificate.getCourse().getTitle()
                : "Khóa học trực tuyến";

        String instructorName = (certificate.getCourse() != null && certificate.getCourse().getInstructor() != null)
                ? certificate.getCourse().getInstructor().getFullName()
                : "Ban Giảng huấn Edumy";

        String code = certificate.getCertificateUrl();
        String verifyUrl = "http://localhost:3000/certificates/verify/" + code;

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
        response.put("certificateId", certificate.getId());
        response.put("issuedAt", certificate.getIssuedAt());
        response.put("studentName", certificate.getUser() != null ? certificate.getUser().getFullName() : null);
        response.put("courseName", certificate.getCourse() != null ? certificate.getCourse().getTitle() : null);
        response.put("courseId", certificate.getCourseId());
        response.put("instructorName", (certificate.getCourse() != null && certificate.getCourse().getInstructor() != null)
                ? certificate.getCourse().getInstructor().getFullName()
                : null);
        response.put("certificateUrl", certificate.getCertificateUrl());
        response.put("pdfDownloadUrl", "/api/certificates/" + certificate.getCertificateUrl() + "/pdf");
        return response;
    }
}

package com.edumy.learning.enrollment.controller;

import com.edumy.learning.enrollment.model.Certificate;
import com.edumy.learning.enrollment.repository.CertificateRepository;
import com.edumy.learning.integration.CertificatePdfData;
import com.edumy.learning.integration.CertificatePdfRenderer;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.response.PageResponse;
import com.edumy.shared.security.SecurityUtils;
import com.edumy.shared.time.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/me/certificates")
@PreAuthorize("isAuthenticated()")
public class MeCertificatesController {

    private final CertificateRepository certificateRepository;
    private final CertificatePdfRenderer certificatePdfRenderer;

    @Value("${edumy.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${edumy.time-zone:Asia/Ho_Chi_Minh}")
    private String timeZone;

    public MeCertificatesController(CertificateRepository certificateRepository,
                                    CertificatePdfRenderer certificatePdfRenderer) {
        this.certificateRepository = certificateRepository;
        this.certificatePdfRenderer = certificatePdfRenderer;
    }

    public record MyCertificateDto(
            String code,
            Integer courseId,
            String courseTitle,
            Instant issuedAt,
            String verifyUrl
    ) {}

    @GetMapping
    public ResponseEntity<PageResponse<MyCertificateDto>> getMyCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Page<Certificate> certPage = certificateRepository.findByUserIdOrderByIssuedAtDesc(
                userId, PageRequest.of(safePage, safeSize));

        String base = frontendBaseUrl != null ? frontendBaseUrl.replaceAll("/+$", "") : "http://localhost:3000";
        Page<MyCertificateDto> mapped = certPage.map(c -> new MyCertificateDto(
                c.getCertificateUrl(),
                c.getCourseId(),
                c.getCourse() != null ? c.getCourse().getTitle() : "Khóa học Edumy",
                TimeUtils.toInstant(c.getIssuedAt(), timeZone),
                base + "/certificates/verify/" + c.getCertificateUrl()
        ));
        return ResponseEntity.ok(PageResponse.from(mapped));
    }

    @GetMapping("/{code}/pdf")
    public ResponseEntity<byte[]> getMyCertificatePdf(@PathVariable String code) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        Certificate certificate = certificateRepository.findByCertificateUrl(code);
        if (certificate == null || !Objects.equals(certificate.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy chứng chỉ.");
        }

        String studentName = (certificate.getUser() != null && certificate.getUser().getFullName() != null)
                ? certificate.getUser().getFullName()
                : "Học viên Edumy";
        String courseTitle = (certificate.getCourse() != null && certificate.getCourse().getTitle() != null)
                ? certificate.getCourse().getTitle()
                : "Khóa học trực tuyến";
        String instructorName = (certificate.getCourse() != null && certificate.getCourse().getInstructor() != null)
                ? certificate.getCourse().getInstructor().getFullName()
                : "Ban Giảng huấn Edumy";

        String base = frontendBaseUrl != null ? frontendBaseUrl.replaceAll("/+$", "") : "http://localhost:3000";
        String verifyUrl = base + "/certificates/verify/" + code;

        CertificatePdfData pdfData = new CertificatePdfData(
                code,
                studentName,
                courseTitle,
                instructorName,
                certificate.getIssuedAt() != null ? certificate.getIssuedAt() : LocalDateTime.now(),
                verifyUrl
        );
        byte[] pdfBytes = certificatePdfRenderer.render(pdfData);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"certificate-" + code + ".pdf\"")
                .body(pdfBytes);
    }
}

package com.edumy.learning.integration;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data needed to render a certificate PDF (BR-40):
 * Student name, course title, instructor name, issue date, code, and verification URL.
 */
public record CertificatePdfData(
        String certificateCode,
        String studentName,
        String courseTitle,
        String instructorName,
        LocalDateTime issuedAt,
        String verifyUrl
) {
    public CertificatePdfData {
        Objects.requireNonNull(certificateCode, "certificateCode is required");
        Objects.requireNonNull(studentName, "studentName is required");
        Objects.requireNonNull(courseTitle, "courseTitle is required");
        Objects.requireNonNull(issuedAt, "issuedAt is required");
        Objects.requireNonNull(verifyUrl, "verifyUrl is required");
    }
}

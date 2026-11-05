package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/Certificates")
public class CertificatesController {

    @Autowired
    private CertificateRepository certificateRepository;

    @GetMapping("/{url}")
    public ResponseEntity<?> getCertificateByUrl(@PathVariable String url) {
        Certificate certificate = certificateRepository.findByCertificateUrl(url);

        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("certificateId", certificate.getId());
        response.put("issuedAt", certificate.getIssuedAt());
        response.put("studentName", certificate.getUser() != null ? certificate.getUser().getFullName() : null);
        response.put("courseName", certificate.getCourse() != null ? certificate.getCourse().getTitle() : null);
        response.put("instructorName", (certificate.getCourse() != null && certificate.getCourse().getInstructor() != null) ? certificate.getCourse().getInstructor().getFullName() : null);
        response.put("certificateUrl", certificate.getCertificateUrl());

        return ResponseEntity.ok(response);
    }
}

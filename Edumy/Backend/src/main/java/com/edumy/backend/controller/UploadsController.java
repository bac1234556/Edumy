package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/Uploads")
public class UploadsController {

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }

        String[] allowedExtensions = { ".jpg", ".jpeg", ".png", ".gif", ".webp" };
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        final String extFinal = extension;
        if (Arrays.stream(allowedExtensions).noneMatch(ext -> ext.equals(extFinal))) {
            return ResponseEntity.badRequest().body("Invalid file type. Only images are allowed.");
        }

        try {
            String uploadsDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
            File dir = new File(uploadsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String safeFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path path = Paths.get(uploadsDir, safeFilename);
            Files.write(path, file.getBytes());

            String fileUrl = "/uploads/" + safeFilename;
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }
}

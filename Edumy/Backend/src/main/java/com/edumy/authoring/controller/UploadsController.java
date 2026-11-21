package com.edumy.authoring.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping({"/api/Uploads", "/api/uploads"})
public class UploadsController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Value("${edumy.uploads.dir:uploads}")
    private String uploadsDir;

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file uploaded."));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid file name."));
        }

        // Strip any path components to prevent traversal
        String baseName = originalFilename.replace("\\", "/");
        int lastSlash = baseName.lastIndexOf('/');
        if (lastSlash >= 0) {
            baseName = baseName.substring(lastSlash + 1);
        }

        String extension = "";
        int lastDot = baseName.lastIndexOf('.');
        if (lastDot >= 0) {
            extension = baseName.substring(lastDot).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid file type. Only images are allowed."));
        }

        try {
            byte[] bytes = file.getBytes();
            if (!hasValidImageMagicBytes(bytes, extension)) {
                return ResponseEntity.badRequest().body(Map.of("message", "File content does not match a valid image signature."));
            }

            Path dir = Paths.get(uploadsDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String safeFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path targetPath = dir.resolve(safeFilename).normalize();
            if (!targetPath.startsWith(dir)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid destination path."));
            }

            Files.write(targetPath, bytes);
            String fileUrl = "/uploads/" + safeFilename;
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error uploading file: " + e.getMessage()));
        }
    }

    static boolean hasValidImageMagicBytes(byte[] bytes, String ext) {
        if (bytes == null || bytes.length < 4) return false;
        // PNG: 89 50 4E 47
        if (".png".equals(ext)) {
            return bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47;
        }
        // JPEG: FF D8 FF
        if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
            return (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
        }
        // GIF: GIF87a / GIF89a
        if (".gif".equals(ext)) {
            return bytes.length >= 6
                    && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8';
        }
        // WEBP: RIFF....WEBP
        if (".webp".equals(ext)) {
            return bytes.length >= 12
                    && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        }
        return false;
    }
}

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
@RequestMapping({"/api/Media", "/api/media"})
public class MediaController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            ".pdf", ".mp4", ".webm", ".mov", ".mp3"
    );

    @Value("${edumy.uploads.dir:uploads}")
    private String uploadsDir;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file uploaded."));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid file name."));
        }

        String baseName = originalFilename.replace("\\", "/");
        int lastSlash = baseName.lastIndexOf('/');
        if (lastSlash >= 0) {
            baseName = baseName.substring(lastSlash + 1);
        }

        String ext = "";
        int lastDot = baseName.lastIndexOf('.');
        if (lastDot >= 0) {
            ext = baseName.substring(lastDot).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Định dạng tập tin không được phép tải lên."));
        }

        try {
            byte[] bytes = file.getBytes();
            if (!hasValidMediaSignature(bytes, ext)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nội dung tập tin không khớp với định dạng cho phép."));
            }

            Path dir = Paths.get(uploadsDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String safeFilename = UUID.randomUUID().toString().replace("-", "") + ext;
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

    private static boolean hasValidMediaSignature(byte[] bytes, String ext) {
        if (bytes == null || bytes.length < 4) return false;
        if (Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(ext)) {
            return UploadsController.hasValidImageMagicBytes(bytes, ext);
        }
        if (".pdf".equals(ext)) {
            return bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
        }
        if (".mp4".equals(ext) || ".mov".equals(ext)) {
            return bytes.length >= 8 && bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p';
        }
        if (".webm".equals(ext)) {
            return (bytes[0] & 0xFF) == 0x1A && (bytes[1] & 0xFF) == 0x45
                    && (bytes[2] & 0xFF) == 0xDF && (bytes[3] & 0xFF) == 0xA3;
        }
        if (".mp3".equals(ext)) {
            boolean id3 = bytes[0] == 'I' && bytes[1] == 'D' && bytes[2] == '3';
            boolean frameSync = (bytes[0] & 0xFF) == 0xFF && ((bytes[1] & 0xE0) == 0xE0);
            return id3 || frameSync;
        }
        return false;
    }
}

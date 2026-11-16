package com.edumy.learning.integration;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * OpenPDF certificate renderer (A4 landscape with Vietnamese typography support).
 */
@Component
public class OpenPdfCertificateRenderer implements CertificatePdfRenderer {

    private static final Logger log = LoggerFactory.getLogger(OpenPdfCertificateRenderer.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color ACCENT_COLOR = new Color(0x56, 0x24, 0xD0);
    private static final Color MUTED_COLOR = new Color(0x55, 0x55, 0x55);

    private final BaseFont regular;
    private final BaseFont bold;

    public OpenPdfCertificateRenderer() {
        BaseFont reg = null;
        BaseFont bld = null;
        try {
            reg = loadFont("fonts/NotoSans-Regular.ttf");
            bld = loadFont("fonts/NotoSans-Bold.ttf");
        } catch (Exception e) {
            log.warn("Could not load NotoSans font from classpath, falling back to standard Helvetica: {}", e.getMessage());
            try {
                reg = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                bld = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            } catch (Exception ex) {
                log.error("Fatal error loading fallback fonts", ex);
            }
        }
        this.regular = reg;
        this.bold = bld;
    }

    @Override
    public byte[] render(CertificatePdfData certificate) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
            Document document = new Document(PageSize.A4.rotate(), 64, 64, 72, 56);
            PdfWriter.getInstance(document, out);
            document.addTitle("Chứng chỉ hoàn thành khóa học - " + certificate.certificateCode());
            document.addCreator("Edumy E-Learning Platform");
            document.open();

            line(document, "EDUMY E-LEARNING", new Font(bold, 16, Font.NORMAL, ACCENT_COLOR), 0);
            line(document, "CHỨNG CHỈ HOÀN THÀNH KHÓA HỌC", new Font(bold, 28, Font.NORMAL, Color.BLACK), 18);
            line(document, "Chứng nhận học viên", new Font(regular, 15, Font.NORMAL, MUTED_COLOR), 28);
            line(document, certificate.studentName(), new Font(bold, 30, Font.NORMAL, ACCENT_COLOR), 10);
            line(document, "đã xuất sắc hoàn thành khóa học trực tuyến", new Font(regular, 15, Font.NORMAL, MUTED_COLOR), 18);
            line(document, certificate.courseTitle(), new Font(bold, 22, Font.NORMAL, Color.BLACK), 8);

            if (certificate.instructorName() != null && !certificate.instructorName().isBlank()) {
                line(document, "Giảng viên: " + certificate.instructorName(),
                        new Font(regular, 14, Font.NORMAL, Color.BLACK), 16);
            }

            line(document, "Ngày cấp: " + DATE_FORMATTER.format(certificate.issuedAt()),
                    new Font(regular, 13, Font.NORMAL, Color.BLACK), 26);
            line(document, "Mã số chứng chỉ: " + certificate.certificateCode(),
                    new Font(bold, 13, Font.NORMAL, Color.BLACK), 6);
            line(document, "Tra cứu xác thực tại: " + certificate.verifyUrl(),
                    new Font(regular, 11, Font.NORMAL, MUTED_COLOR), 6);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new CertificateRenderingException("Không thể tạo file PDF chứng chỉ: " + e.getMessage(), e);
        }
    }

    private static void line(Document document, String text, Font font, float spacingBefore) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(spacingBefore);
        document.add(paragraph);
    }

    private static BaseFont loadFont(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            byte[] bytes = in.readAllBytes();
            return BaseFont.createFont(path.substring(path.lastIndexOf('/') + 1), BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, BaseFont.CACHED, bytes, null);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc font: " + path, e);
        }
    }
}

package com.skillbridge.lms.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.skillbridge.lms.dto.response.CertificateResponse;
import com.skillbridge.lms.entity.Certificate;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.CertificateRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates(String userEmail) {
        User user = findUserByEmail(userEmail);
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(user.getId()).stream()
                .map(CertificateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(Long id, String userEmail) {
        User user = findUserByEmail(userEmail);
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("証明書が見つかりません: " + id));

        if (!certificate.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("この証明書にアクセスする権限がありません");
        }

        return CertificateResponse.from(certificate);
    }

    @Transactional
    public Certificate issueCertificate(User user, com.skillbridge.lms.entity.Course course) {
        if (certificateRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            return certificateRepository.findByUserIdAndCourseId(user.getId(), course.getId()).orElse(null);
        }

        Certificate certificate = Certificate.builder()
                .user(user)
                .course(course)
                .certificateNumber(UUID.randomUUID().toString())
                .build();

        return certificateRepository.save(certificate);
    }

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id, String userEmail) {
        User user = findUserByEmail(userEmail);
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("証明書が見つかりません: " + id));

        if (!certificate.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("この証明書にアクセスする権限がありません");
        }

        return createPdfBytes(certificate);
    }

    private byte[] createPdfBytes(Certificate certificate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 36, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 18, Font.NORMAL);
            Font nameFont = new Font(Font.HELVETICA, 28, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 14, Font.NORMAL);

            document.add(createCenteredParagraph("Certificate of Completion", titleFont, 60f));
            document.add(createCenteredParagraph("This is to certify that", subtitleFont, 30f));
            document.add(createCenteredParagraph(certificate.getUser().getUsername(), nameFont, 30f));
            document.add(createCenteredParagraph(
                    "has successfully completed the course", subtitleFont, 30f));
            document.add(createCenteredParagraph(
                    certificate.getCourse().getTitle(), nameFont, 40f));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            document.add(createCenteredParagraph(
                    "Date: " + certificate.getIssuedAt().format(formatter), bodyFont, 20f));
            document.add(createCenteredParagraph(
                    "Certificate No: " + certificate.getCertificateNumber(), bodyFont, 10f));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF生成に失敗しました", e);
        }
    }

    private Paragraph createCenteredParagraph(String text, Font font, float spacingAfter) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(spacingAfter);
        return paragraph;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
    }
}

package com.skillbridge.lms.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.response.CertificateResponse;
import com.skillbridge.lms.service.CertificateService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "証明書管理 API")
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/my")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(certificateService.getMyCertificates(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> getCertificate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(certificateService.getCertificate(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] pdfBytes = certificateService.generatePdf(id, userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}

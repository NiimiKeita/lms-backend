package com.skillbridge.lms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@skillbridge.com}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("【SkillBridge】パスワードリセット");
        message.setText(
                "パスワードリセットのリクエストを受け付けました。\n\n"
                + "以下のリンクからパスワードをリセットしてください：\n"
                + resetLink + "\n\n"
                + "このリンクは1時間有効です。\n"
                + "心当たりがない場合は、このメールを無視してください。\n\n"
                + "SkillBridge LMS"
        );

        try {
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to: {}", toEmail, e);
        }
    }
}

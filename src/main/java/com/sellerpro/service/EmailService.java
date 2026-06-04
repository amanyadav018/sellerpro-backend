package com.sellerpro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: #4F46E5; padding: 20px; text-align: center;">
                <h1 style="color: white; margin: 0;">SellerPro</h1>
              </div>
              <div style="padding: 30px; background: #f9f9f9;">
                <h2>Hello %s,</h2>
                <p>Welcome to SellerPro! Please verify your email to get started.</p>
                <a href="%s" style="display: inline-block; background: #4F46E5; color: white;
                   padding: 12px 30px; border-radius: 6px; text-decoration: none; font-size: 16px;">
                  Verify Email
                </a>
                <p style="color: #666; margin-top: 20px;">
                  This link expires in 24 hours. If you didn't create an account, ignore this email.
                </p>
              </div>
            </div>
            """.formatted(fullName, verifyUrl);

        sendHtmlEmail(toEmail, "Verify your SellerPro account", html);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: #4F46E5; padding: 20px; text-align: center;">
                <h1 style="color: white; margin: 0;">SellerPro</h1>
              </div>
              <div style="padding: 30px; background: #f9f9f9;">
                <h2>Hello %s,</h2>
                <p>We received a request to reset your password.</p>
                <a href="%s" style="display: inline-block; background: #EF4444; color: white;
                   padding: 12px 30px; border-radius: 6px; text-decoration: none; font-size: 16px;">
                  Reset Password
                </a>
                <p style="color: #666; margin-top: 20px;">
                  This link expires in 1 hour. If you didn't request this, please ignore.
                </p>
              </div>
            </div>
            """.formatted(fullName, resetUrl);

        sendHtmlEmail(toEmail, "Reset your SellerPro password", html);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: #4F46E5; padding: 20px; text-align: center;">
                <h1 style="color: white; margin: 0;">SellerPro</h1>
              </div>
              <div style="padding: 30px; background: #f9f9f9;">
                <h2>Welcome aboard, %s! 🎉</h2>
                <p>Your account is verified. Start uploading your sales data to get insights.</p>
                <a href="%s/dashboard" style="display: inline-block; background: #4F46E5; color: white;
                   padding: 12px 30px; border-radius: 6px; text-decoration: none; font-size: 16px;">
                  Go to Dashboard
                </a>
              </div>
            </div>
            """.formatted(fullName, frontendUrl);

        sendHtmlEmail(toEmail, "Welcome to SellerPro!", html);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "SellerPro");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email send failed", e);
        }
    }
}

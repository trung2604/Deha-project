package com.deha.HumanResourceManagement.service.support;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.mail.fail-fast:false}")
    private boolean mailFailFast;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendVerificationEmail(String to, String fullName, String token) {
        Context ctx = new Context();
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("email", to);
        ctx.setVariable("verifyLink", frontendUrl + "/verify-email?token=" + token);

        String html = templateEngine.process("verification", ctx);
        send(to, "[HRM] Kích hoạt tài khoản của bạn", html);
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        Context ctx = new Context();
        ctx.setVariable("email", to);
        ctx.setVariable("otp", otp);

        String html = templateEngine.process("otp", ctx);
        send(to, "[HRM] Mã OTP đặt lại mật khẩu", html);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            if (mailFailFast) {
                log.error("Failed to send email to {} with subject '{}': {}", to, subject, e.getMessage(), e);
                throw new IllegalStateException("Email delivery failed", e);
            }
            // In non fail-fast mode, keep logs concise to avoid stacktrace noise on known TLS/environment issues.
            log.warn("Email not sent to {} (subject='{}'): {}", to, subject, e.getMessage());
        }
    }
}
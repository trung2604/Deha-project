package com.deha.HumanResourceManagement.service.support;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final TemplateEngine templateEngine;
    private final SendGrid sendGrid;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.mail.fail-fast:true}")
    private boolean mailFailFast;

    public EmailService(TemplateEngine templateEngine,
                        @Value("${app.sendgrid.api-key}") String apiKey) {
        this.templateEngine = templateEngine;
        this.sendGrid = new SendGrid(apiKey);
    }

    @Async("mailTaskExecutor")
    public void sendVerificationEmail(String to, String fullName, String token) {
        if (frontendUrl == null || frontendUrl.isBlank()) {
            throw new IllegalStateException("Missing app.frontend.url");
        }
        Context ctx = new Context();
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("email", to);
        ctx.setVariable("verifyLink", frontendUrl + "/verify-email?token=" + token);

        String html = templateEngine.process("verification", ctx);
        send(to, "[HRM] Kích hoạt tài khoản của bạn", html);
    }

    @Async("mailTaskExecutor")
    public void sendOtpEmail(String to, String otp) {
        Context ctx = new Context();
        ctx.setVariable("email", to);
        ctx.setVariable("otp", otp);

        String html = templateEngine.process("otp", ctx);
        send(to, "[HRM] Mã OTP đặt lại mật khẩu", html);
    }

    private void send(String to, String subject, String html) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Missing app.mail.from configuration");
        }
        try {
            Email from = new Email(fromAddress);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", html);
            Mail mail = new Mail(from, subject, toEmail, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent via SendGrid to {} (subject='{}')", to, subject);
            } else {
                handleFailure(to, subject,
                        "SendGrid returned status " + response.getStatusCode()
                                + ": " + response.getBody());
            }
        } catch (IOException e) {
            handleFailure(to, subject, e.getMessage());
        }
    }

    private void handleFailure(String to, String subject, String reason) {
        if (mailFailFast) {
            log.error("Failed to send email to {} (subject='{}'): {}", to, subject, reason);
            throw new IllegalStateException("Email delivery failed: " + reason);
        }
        log.warn("Email not sent to {} (subject='{}'): {}", to, subject, reason);
    }
}
package com.internship.tool.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:3000}")
    private String appBaseUrl;

    /**
     * Sends an HTML email asynchronously using a Thymeleaf template.
     * Marked @Async so that the calling request is not blocked.
     *
     * @param to           recipient email address
     * @param subject      email subject
     * @param templateName Thymeleaf template name (without .html)
     * @param variables    variables to pass to the template context
     */
    @Async
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            variables.put("appUrl", appBaseUrl);

            Context context = new Context();
            context.setVariables(variables);

            String htmlBody = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email sent successfully to {} with subject: {}", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to {} with subject: {}. Error: {}", to, subject, e.getMessage());
            // Do NOT re-throw — email failures should not fail the main request
        }
    }
}

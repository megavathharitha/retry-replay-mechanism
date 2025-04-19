package com.example.transactionretryreplay.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailTemplateConfig emailTemplateConfig;

    public void sendEmail(String to, String subjectTemplate, String templateName, Map<String, Object> model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);

            // Process the subject template with the model
            Context subjectContext = new Context();
            subjectContext.setVariables(model);
            String subject = templateEngine.process(subjectTemplate, subjectContext);
            helper.setSubject(subject);

            helper.setFrom(emailTemplateConfig.getDefaultFrom());

            if (emailTemplateConfig.getDefaultCc() != null && !emailTemplateConfig.getDefaultCc().isEmpty()) {
                helper.setCc(Stream.of(emailTemplateConfig.getDefaultCc().split(","))
                        .map(String::trim)
                        .toArray(String[]::new));
            }
            if (emailTemplateConfig.getDefaultBcc() != null && !emailTemplateConfig.getDefaultBcc().isEmpty()) {
                helper.setBcc(Stream.of(emailTemplateConfig.getDefaultBcc().split(","))
                        .map(String::trim)
                        .toArray(String[]::new));
            }

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(model);
            String htmlBody = templateEngine.process("emails/" + templateName, thymeleafContext);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);

        } catch (MessagingException e) {
            log.error("Error sending email to {}: {}", to, e.getMessage());
        }
    }
}
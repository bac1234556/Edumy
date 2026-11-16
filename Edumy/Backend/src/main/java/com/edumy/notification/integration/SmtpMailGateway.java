package com.edumy.notification.integration;

import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Sends UTF-8 HTML mails through Spring Mail (JavaMailSender / Mailpit).
 */
@Component
public class SmtpMailGateway implements MailGateway {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailGateway.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public SmtpMailGateway(ObjectProvider<JavaMailSender> mailSender,
                           @Value("${edumy.mail.from:Edumy Academy <no-reply@edumy.vn>}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(MailMessage message) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Mail dropped, no mail host configured: template={}", message.template());
            return;
        }
        MimeMessage mime = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(message.to());
            mime.setSubject(message.subject(), StandardCharsets.UTF_8.name());
            helper.setText(message.htmlBody(), true);
        } catch (MessagingException e) {
            throw new MailPreparationException("Could not build mail: template=" + message.template(), e);
        }
        sender.send(mime);
        log.info("Email delivered to SMTP successfully: template={}, subject={}", message.template(), message.subject());
    }
}

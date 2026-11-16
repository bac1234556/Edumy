package com.edumy.notification.integration;

import java.util.Objects;

/**
 * One composed HTML email (UTF-8).
 */
public record MailMessage(String template, String to, String subject, String htmlBody) {

    public MailMessage {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(htmlBody, "htmlBody");
    }

    @Override
    public String toString() {
        return "MailMessage[template=" + template + ", to=****, subject=" + subject + ", htmlBody=****]";
    }
}

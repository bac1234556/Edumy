package com.edumy.notification.integration;

/**
 * Gate to send a single composed HTML email.
 */
public interface MailGateway {
    void send(MailMessage message);
}

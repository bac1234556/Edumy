package com.edumy.notification.listener;

import com.edumy.identity.api.EmailVerificationRequested;
import com.edumy.identity.api.PasswordChanged;
import com.edumy.identity.api.PasswordResetRequested;
import com.edumy.identity.api.UserRegistered;
import com.edumy.notification.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Mails of the identity flows (SRS §7 mail list), sent asynchronously after the publishing transaction committed:
 * {@code @Async} + {@code @TransactionalEventListener} (AFTER_COMMIT).
 */
@Component
public class IdentityMailListener {

    private static final Logger log = LoggerFactory.getLogger(IdentityMailListener.class);

    private final MailService mails;

    public IdentityMailListener(MailService mails) {
        this.mails = mails;
    }

    @Async
    @TransactionalEventListener
    public void on(UserRegistered event) {
        deliver("UserRegistered", event.userId(),
                () -> mails.sendEmailVerification(event.email(), event.fullName(), event.rawVerificationCode()));
    }

    @Async
    @TransactionalEventListener
    public void on(EmailVerificationRequested event) {
        deliver("EmailVerificationRequested", event.userId(),
                () -> mails.sendEmailVerification(event.email(), event.fullName(), event.rawCode()));
    }

    @Async
    @TransactionalEventListener
    public void on(PasswordResetRequested event) {
        deliver("PasswordResetRequested", event.userId(),
                () -> mails.sendPasswordReset(event.email(), event.fullName(), event.rawCode()));
    }

    @Async
    @TransactionalEventListener
    public void on(PasswordChanged event) {
        deliver("PasswordChanged", event.userId(),
                () -> mails.sendPasswordChanged(event.email(), event.fullName(), event.occurredAt()));
    }

    private static void deliver(String eventType, Long userId, Runnable send) {
        try {
            send.run();
        } catch (RuntimeException e) {
            Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
            log.error("Mail not sent: event={}, userId={}, error={}, cause={}", eventType, userId,
                    e.getClass().getName(), cause.getClass().getName());
        }
    }
}

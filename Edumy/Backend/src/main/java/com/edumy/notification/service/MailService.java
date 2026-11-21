package com.edumy.notification.service;

import java.time.Instant;
import java.util.List;

import com.edumy.notification.integration.MailGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * High-level mail dispatch service for Edumy notifications.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final MailComposer composer;
    private final MailGateway gateway;

    public MailService(MailComposer composer, MailGateway gateway) {
        this.composer = composer;
        this.gateway = gateway;
    }

    /** Send email verification link. */
    @Async
    public void sendEmailVerification(String to, String fullName, String rawCode) {
        try {
            gateway.send(composer.emailVerification(to, fullName, rawCode));
        } catch (Exception e) {
            log.error("Failed to send email verification to {}: {}", to, e.getMessage());
        }
    }

    /** Send password reset link. */
    @Async
    public void sendPasswordReset(String to, String fullName, String rawCode) {
        try {
            gateway.send(composer.passwordReset(to, fullName, rawCode));
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    /** Send password changed notification. */
    @Async
    public void sendPasswordChanged(String to, String fullName, Instant changedAt) {
        try {
            gateway.send(composer.passwordChanged(to, fullName, changedAt));
        } catch (Exception e) {
            log.error("Failed to send password changed notice to {}: {}", to, e.getMessage());
        }
    }

    /** Send co-instructor invitation. */
    @Async
    public void sendCoInstructorInvited(String to, String fullName, String inviterName, String courseTitle) {
        try {
            gateway.send(composer.coInstructorInvited(to, fullName, inviterName, courseTitle));
        } catch (Exception e) {
            log.error("Failed to send co-instructor invitation to {}: {}", to, e.getMessage());
        }
    }

    /** Send question answered notification. */
    @Async
    public void sendQuestionAnswered(String to, String fullName, String instructorName, String questionTitle,
                                     String courseTitle, String slug, Long questionId) {
        try {
            gateway.send(composer.questionAnswered(to, fullName, instructorName, questionTitle, courseTitle, slug, questionId));
        } catch (Exception e) {
            log.error("Failed to send question answered email to {}: {}", to, e.getMessage());
        }
    }

    /** Send course approved notice. */
    @Async
    public void sendCourseApproved(String to, String fullName, String courseTitle, String slug) {
        try {
            gateway.send(composer.courseApproved(to, fullName, courseTitle, slug));
        } catch (Exception e) {
            log.error("Failed to send course approved email to {}: {}", to, e.getMessage());
        }
    }

    /** Send course rejected notice with review reason. */
    @Async
    public void sendCourseRejected(String to, String fullName, String courseTitle, Long courseId, String reason) {
        try {
            gateway.send(composer.courseRejected(to, fullName, courseTitle, courseId, reason));
        } catch (Exception e) {
            log.error("Failed to send course rejected email to {}: {}", to, e.getMessage());
        }
    }

    /** Send course hidden notice. */
    @Async
    public void sendCourseHidden(String to, String fullName, String courseTitle, Long courseId, String reason) {
        try {
            gateway.send(composer.courseHidden(to, fullName, courseTitle, courseId, reason));
        } catch (Exception e) {
            log.error("Failed to send course hidden email to {}: {}", to, e.getMessage());
        }
    }

    /** Send purchase confirmation. */
    @Async
    public void sendPurchaseConfirmed(String to, String fullName, String orderCode, long totalAmount,
                                      List<String> courseTitles) {
        try {
            gateway.send(composer.purchaseConfirmed(to, fullName, orderCode, totalAmount, courseTitles));
        } catch (Exception e) {
            log.error("Failed to send purchase confirmation to {}: {}", to, e.getMessage());
        }
    }

    /** Send payment reconciliation result (UC-A18). */
    @Async
    public void sendPaymentReconciled(String to, String fullName, String orderCode, boolean activated,
                                      List<String> courseTitles) {
        try {
            gateway.send(composer.paymentReconciled(to, fullName, orderCode, activated, courseTitles));
        } catch (Exception e) {
            log.error("Failed to send payment reconciliation email to {}: {}", to, e.getMessage());
        }
    }
}

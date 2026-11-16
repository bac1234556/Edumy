/**
 * Notification module: Mailpit SMTP, transactional HTML templates, async email dispatch.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Notification",
        allowedDependencies = {"shared", "identity :: api", "authoring :: api", "commerce :: api", "learning :: api"})
package com.edumy.notification;

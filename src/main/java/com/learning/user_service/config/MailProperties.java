package com.learning.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Outgoing email. provider=log prints mails to the log; provider=brevo sends them through Brevo's HTTPS API. */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(String provider, String fromAddress, String fromName, String brevoApiKey,
                             String consoleUrl) {
}

package com.learning.user_service.gateway.messaging;

import com.learning.user_service.config.MailProperties;
import com.learning.user_service.exception.NonRetryablePublishException;
import com.learning.user_service.gateway.MailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends through Brevo's transactional email API over HTTPS. HTTPS is used on purpose: Render's free
 * tier blocks outbound SMTP ports. The sender address must be verified in the Brevo account.
 */
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "brevo")
public class BrevoMailSender implements MailSender {

    private final RestClient client;
    private final Map<String, String> sender;

    public BrevoMailSender(MailProperties properties, RestClient.Builder builder) {
        if (isBlank(properties.brevoApiKey()) || isBlank(properties.fromAddress())) {
            throw new IllegalStateException("app.mail.provider=brevo needs BREVO_API_KEY and MAIL_FROM_ADDRESS");
        }
        this.client = builder.baseUrl("https://api.brevo.com/v3").defaultHeader("api-key", properties.brevoApiKey()).build();
        this.sender = Map.of("name", properties.fromName(), "email", properties.fromAddress());
    }

    @Override
    public void send(String toAddress, String toName, String subject, String html, String text) {
        Map<String, Object> body = Map.of(
                "sender", sender,
                "to", List.of(Map.of("email", toAddress, "name", toName)),
                "subject", subject,
                "htmlContent", html,
                "textContent", text);
        try {
            client.post().uri("/smtp/email").body(body).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            HttpStatusCode status = ex.getStatusCode();
            if (status.value() == 429) {
                throw ex; // rate limited: retry later
            }
            throw new NonRetryablePublishException("Brevo rejected the mail (" + status.value() + "): "
                    + ex.getResponseBodyAsString(), ex);
        }
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}

package com.learning.user_service.core.support;

import com.learning.user_service.config.MailProperties;
import com.learning.user_service.gateway.MailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Turns outbox events into emails: welcome, email verification and password reset. Links open the
 * account console, which reads the one-time token from the query string.
 */
@Component
public class UserMailNotifier {

    private final MailSender mailSender;
    private final JsonMapper jsonMapper;
    private final String consoleUrl;

    public UserMailNotifier(MailSender mailSender, JsonMapper jsonMapper, MailProperties properties) {
        this.mailSender = mailSender;
        this.jsonMapper = jsonMapper;
        this.consoleUrl = properties.consoleUrl() == null ? "" : properties.consoleUrl().replaceAll("/+$", "");
    }

    /** Sends the mail that goes with this event, if it has one. Throws when the provider fails. */
    public void notify(String eventType, String envelopeJson) {
        JsonNode data = jsonMapper.readTree(envelopeJson).path("data");
        String email = data.path("email").asString("");
        if (email.isBlank()) {
            return;
        }
        String name = data.path("firstName").asString("there");
        switch (eventType) {
            case "USER_REGISTERED" -> send(email, name, "Welcome to Bazaar",
                    "Welcome, " + name + "!",
                    "Your account is ready. We sent a separate email to confirm your address; confirming it keeps your account secure.",
                    "Open your account", consoleUrl + "/console.html", "You can sign in any time at");
            case "EMAIL_VERIFICATION_REQUESTED" -> {
                String token = data.path("verificationToken").asString("");
                send(email, name, "Confirm your email address", "Confirm your email",
                        "Hi " + name + ", confirm this address to finish setting up your account. The link works for 24 hours.",
                        "Confirm email", consoleUrl + "/console.html?verify=" + enc(token), "Or paste this link into your browser:");
            }
            case "PASSWORD_RESET_REQUESTED" -> {
                String token = data.path("resetToken").asString("");
                send(email, name, "Reset your password", "Reset your password",
                        "Hi " + name + ", use the button to choose a new password. The link works for 30 minutes. If you did not ask for this, ignore this email; your password stays the same.",
                        "Choose a new password", consoleUrl + "/console.html?reset=" + enc(token), "Or paste this link into your browser:");
            }
            default -> { /* no mail for other events */ }
        }
    }

    /** Mail for an ORDER_CONFIRMED event from the order service (envelope data already parsed). */
    public void orderConfirmed(String email, String name, JsonNode data) {
        String number = data.path("orderNumber").asString("");
        String currency = data.path("currency").asString("");
        StringBuilder lines = new StringBuilder();
        for (JsonNode item : data.path("items")) {
            lines.append("- ").append(item.path("quantity").asInt(1)).append(" x ")
                    .append(item.path("title").asString("item")).append(" (").append(currency).append(' ')
                    .append(item.path("unitPrice").asString("")).append(")\n");
        }
        String body = "Hi " + name + ", your order " + number + " is confirmed and paid.\n\n" + lines
                + "\nTotal: " + currency + " " + data.path("total").asString("");
        send(email, name, "Order " + number + " confirmed", "Your order is confirmed", body, null, null, null);
    }

    private void send(String email, String name, String subject, String heading, String body,
                      String button, String link, String linkHint) {
        String html = "<div style=\"font-family:-apple-system,Segoe UI,Roboto,sans-serif;max-width:480px;margin:auto;padding:24px;color:#17211f\">"
                + "<h2 style=\"margin:0 0 12px\">" + HtmlUtils.htmlEscape(heading) + "</h2>"
                + "<p style=\"line-height:1.5;white-space:pre-line\">" + HtmlUtils.htmlEscape(body) + "</p>"
                + (link == null ? "" : "<p><a href=\"" + HtmlUtils.htmlEscape(link) + "\" style=\"display:inline-block;background:#0d6b5f;color:#fff;"
                + "padding:10px 18px;border-radius:8px;text-decoration:none;font-weight:600\">" + HtmlUtils.htmlEscape(button) + "</a></p>"
                + "<p style=\"font-size:12px;color:#5b6a66\">" + HtmlUtils.htmlEscape(linkHint) + "<br>" + HtmlUtils.htmlEscape(link) + "</p>")
                + "</div>";
        String text = heading + "\n\n" + body + "\n" + (link == null ? "" : "\n" + linkHint + "\n" + link + "\n");
        mailSender.send(email, name, subject, html, text);
    }

    private static String enc(String token) {
        return URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}

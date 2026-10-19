package com.learning.user_service.core.support;

import com.learning.user_service.config.MailProperties;
import com.learning.user_service.gateway.MailSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserMailNotifierTest {

    private final MailSender sender = mock(MailSender.class);
    private final UserMailNotifier notifier = new UserMailNotifier(sender, JsonMapper.builder().build(),
            new MailProperties("log", "no-reply@example.com", "ShopTrail", "", "https://users.example.com/"));

    private static String envelope(String data) {
        return "{\"eventType\":\"X\",\"data\":" + data + "}";
    }

    @Test
    void verificationMailCarriesEncodedTokenLink() {
        notifier.notify("EMAIL_VERIFICATION_REQUESTED",
                envelope("{\"email\":\"a@example.com\",\"firstName\":\"<b>Asha</b>\",\"verificationToken\":\"ab-_c+d\"}"));
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq("a@example.com"), eq("<b>Asha</b>"), eq("Confirm your email address"), html.capture(), text.capture());
        assertThat(text.getValue()).contains("https://users.example.com/console.html?verify=ab-_c%2Bd");
        assertThat(html.getValue()).doesNotContain("<b>Asha</b>").contains("&lt;b&gt;Asha&lt;/b&gt;");
    }

    @Test
    void resetAndWelcomeMailsAreSent() {
        notifier.notify("PASSWORD_RESET_REQUESTED", envelope("{\"email\":\"a@example.com\",\"firstName\":\"Asha\",\"resetToken\":\"tok\"}"));
        verify(sender).send(eq("a@example.com"), eq("Asha"), eq("Reset your password"), any(), any());
        notifier.notify("USER_REGISTERED", envelope("{\"email\":\"a@example.com\",\"firstName\":\"Asha\"}"));
        verify(sender).send(eq("a@example.com"), eq("Asha"), eq("Welcome to ShopTrail"), any(), any());
    }

    @Test
    void otherEventsSendNothing() {
        notifier.notify("ADDRESS_UPDATED", envelope("{\"email\":\"a@example.com\"}"));
        verify(sender, never()).send(any(), any(), any(), any(), any());
    }

    @Test
    void orderConfirmedMailListsItemsAndTotal() {
        var data = JsonMapper.builder().build().readTree(
                "{\"orderNumber\":\"ORD-1\",\"currency\":\"INR\",\"total\":\"1998.00\","
                + "\"items\":[{\"title\":\"Tea\",\"quantity\":2,\"unitPrice\":999}]}");
        notifier.orderConfirmed("a@example.com", "Asha", data);
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq("a@example.com"), eq("Asha"), eq("Order ORD-1 confirmed"), any(), text.capture());
        assertThat(text.getValue()).contains("2 x Tea (INR 999)").contains("Total: INR 1998.00");
    }
}

package com.learning.user_service.gateway.messaging;

import com.learning.user_service.config.MailProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;

class BrevoMailSenderTest {

    @EnableConfigurationProperties(MailProperties.class)
    static class Config {
    }

    @Test
    void startsWithBrevoProviderAndNoOtherWebBeans() {
        new ApplicationContextRunner()
                .withUserConfiguration(Config.class, BrevoMailSender.class)
                .withPropertyValues("app.mail.provider=brevo", "app.mail.brevo-api-key=xkeysib-test",
                        "app.mail.from-address=no-reply@example.com", "app.mail.from-name=ShopTrail")
                .run(ctx -> assertThat(ctx).hasSingleBean(BrevoMailSender.class));
    }

    @Test
    void refusesToStartWithoutKey() {
        new ApplicationContextRunner()
                .withUserConfiguration(Config.class, BrevoMailSender.class)
                .withPropertyValues("app.mail.provider=brevo", "app.mail.from-address=no-reply@example.com",
                        "app.mail.from-name=ShopTrail")
                .run(ctx -> assertThat(ctx).hasFailed());
    }
}

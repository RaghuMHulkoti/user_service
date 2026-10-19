package com.learning.user_service.gateway.messaging;

import com.learning.user_service.gateway.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default sender: prints the mail to the log so the service runs without an email account. */
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "log", matchIfMissing = true)
public class LoggingMailSender implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailSender.class);

    @Override
    public void send(String toAddress, String toName, String subject, String html, String text) {
        log.info("MAIL to={} subject=\"{}\"\n{}", toAddress, subject, text);
    }
}

package com.learning.user_service.gateway;

/** Outbound port for sending one email. */
public interface MailSender {

    /**
     * @throws com.learning.user_service.exception.NonRetryablePublishException when the provider will never accept it
     *         (bad address, rejected sender or key)
     * @throws RuntimeException for any other (retryable) failure
     */
    void send(String toAddress, String toName, String subject, String html, String text);
}

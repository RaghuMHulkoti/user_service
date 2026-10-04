package com.learning.user_service.application.support;

import java.util.Locale;

/** Emails are stored and compared lower-case (the unique index is on LOWER(email)). */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}

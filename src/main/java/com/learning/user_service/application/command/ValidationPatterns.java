package com.learning.user_service.application.command;

/** Shared regexes for request validation (TDD §11.2). */
public final class ValidationPatterns {

    /** E.164: '+', country code, up to 15 digits. */
    public static final String E164_PHONE = "^\\+[1-9]\\d{6,14}$";
    /** Practical RFC 5322 subset; requires a dotted domain. */
    public static final String EMAIL = "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$";
    /** BCP 47 style, e.g. en-IN. */
    public static final String LOCALE = "^[a-z]{2,3}(-[A-Z]{2})?$";
    /** ISO 3166-1 alpha-2. */
    public static final String COUNTRY = "^[A-Z]{2}$";
    public static final String CONSENT_KIND = "^[A-Z][A-Z_]{1,39}$";

    private ValidationPatterns() {
    }
}

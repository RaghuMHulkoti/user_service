package com.learning.user_service.domain.policy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {"Str0ng-Passw0rd!", "abcdefghijK1", "ABCDEFGHIJK!1", "lowercase-only-123"})
    void acceptsLongPasswordsWithThreeClasses(String password) {
        assertThat(PasswordPolicy.violations(password)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Sh0rt!", "Elevenchar1"})
    void rejectsShortPasswords(String password) {
        assertThat(PasswordPolicy.violations(password)).singleElement().asString().contains("at least 12");
    }

    @ParameterizedTest
    @ValueSource(strings = {"alllowercaseletters", "ALLUPPERCASE1234", "123456789012345"})
    void rejectsPasswordsWithoutClassMix(String password) {
        assertThat(PasswordPolicy.violations(password)).singleElement().asString().contains("mix");
    }

    @Test
    void rejectsPasswordsLongerThanBcryptLimit() {
        assertThat(PasswordPolicy.violations("Aa1!" + "x".repeat(70))).anyMatch(v -> v.contains("at most 72"));
    }
}

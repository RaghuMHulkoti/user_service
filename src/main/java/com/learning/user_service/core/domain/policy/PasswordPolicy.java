package com.learning.user_service.core.domain.policy;

import java.util.ArrayList;
import java.util.List;

/**
 * Password rule from TDD §11.2: at least 12 characters with a class mix.
 * "Class mix" here means at least 3 of: lower case, upper case, digit, symbol.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 12;
    public static final int MAX_LENGTH = 72; // bcrypt ignores bytes after 72
    private static final int MIN_CLASSES = 3;

    private PasswordPolicy() {
    }

    public static List<String> violations(String password) {
        List<String> problems = new ArrayList<>();
        if (password == null || password.length() < MIN_LENGTH) {
            problems.add("must be at least " + MIN_LENGTH + " characters");
            return problems;
        }
        if (password.length() > MAX_LENGTH) {
            problems.add("must be at most " + MAX_LENGTH + " characters");
        }
        int classes = 0;
        if (password.chars().anyMatch(Character::isLowerCase)) classes++;
        if (password.chars().anyMatch(Character::isUpperCase)) classes++;
        if (password.chars().anyMatch(Character::isDigit)) classes++;
        if (password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) classes++;
        if (classes < MIN_CLASSES) {
            problems.add("must mix at least " + MIN_CLASSES + " of: lower case, upper case, digit, symbol");
        }
        return problems;
    }
}

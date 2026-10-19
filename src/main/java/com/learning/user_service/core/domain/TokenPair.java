package com.learning.user_service.core.domain;

public record TokenPair(String accessToken, String refreshToken, String tokenType,
                        long expiresIn, long refreshExpiresIn) {
}

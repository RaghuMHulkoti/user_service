package com.learning.user_service.application.query;

public record TokenPair(String accessToken, String refreshToken, String tokenType,
                        long expiresIn, long refreshExpiresIn) {
}

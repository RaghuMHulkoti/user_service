package com.learning.user_service.gateway.oracle.idempotency.entity;

import java.io.Serializable;

public record IdempotencyKeyId(String scope, String key) implements Serializable {
}

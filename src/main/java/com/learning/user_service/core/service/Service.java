package com.learning.user_service.core.service;

public interface Service<T extends ServiceRequest, R extends ServiceResponse> {
    R execute(T request);
}

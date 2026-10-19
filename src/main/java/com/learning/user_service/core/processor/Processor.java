package com.learning.user_service.core.processor;

public interface Processor<T extends ProcessorRequest, R extends ProcessorResponse> {
    R process(T request);
}

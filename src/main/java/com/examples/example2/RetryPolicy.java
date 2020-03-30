package com.examples.example2;

import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Optional;

import static com.examples.common.ErrorHeader.X_EXCEPTION_CAUSE;

public class RetryPolicy {

    private Integer maxAttempts;

    private List<String> errorTypes;

    public RetryPolicy(PolicyProperties properties) {
        this.maxAttempts = properties.getMaxAttempts();
        this.errorTypes = properties.getErrorTypes();
    }

    public Boolean isRetryable(Message message) {
        return MessageUtils.getRetriesCounter(message) < maxAttempts && isRetryableError(message);
    }

    private Boolean isRetryableError(Message message) {
        if (errorTypes.isEmpty()) return true;
        else return getErrorCause(message)
                .map(errorCause -> errorTypes.contains(errorCause))
                .orElse(false);
    }

    private Optional<String> getErrorCause(Message message) {
        return Optional.ofNullable(message.getMessageProperties().getHeader(X_EXCEPTION_CAUSE));
    }
}

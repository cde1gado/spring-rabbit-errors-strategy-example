package com.example;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;

import java.util.Map;

public class ErrorRecoverer extends RepublishMessageRecoverer {

    private static final String X_ORIGINAL_QUEUE = "x-original-queue";

    private static final String X_EXCEPTION_CAUSE = "x-exception-cause";

    public ErrorRecoverer(AmqpTemplate errorTemplate, String errorExchange, String errorRoutingKey) {
        super(errorTemplate, errorExchange, errorRoutingKey);
    }

    @Override
    protected Map<? extends String, ?> additionalHeaders(Message message, Throwable error) {
        return Map.of(
                X_EXCEPTION_CAUSE, error.getCause() != null ? error.getCause().getClass() : error.getClass(),
                X_ORIGINAL_QUEUE, message.getMessageProperties().getConsumerQueue()
        );
    }
}

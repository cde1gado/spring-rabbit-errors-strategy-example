package com.examples.common;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;

import java.util.Map;

import static com.examples.common.ErrorHeader.X_EXCEPTION_CAUSE;
import static com.examples.common.ErrorHeader.X_ORIGINAL_QUEUE;

public class ErrorRecoverer extends RepublishMessageRecoverer {

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

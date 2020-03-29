package com.examples.example2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static com.examples.common.ErrorHeader.*;

public class RetrySenderListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrySenderListener.class);

    private static final String X_RETRIES = "x-retries";

    private static final String NAMELESS_EXCHANGE = "";

    private RabbitTemplate rabbitTemplate;

    public RetrySenderListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${rabbit.retry.sender.queue}", durable = "true"),
                    exchange = @Exchange(value = "${rabbit.exchange.errors}"),
                    key = "${rabbit.retry.sender.queue}"
            ),
            containerFactory = "retryContainerFactory"
    )
    public void onMessage(Message message) {
        LOGGER.info("Retrying message");
        retry(message);
    }

    private void retry(Message message) {
        getOriginalQueue(message).ifPresent(originalQueue -> retry(message, originalQueue));
    }

    private void retry(Message message, String originalQueue) {
        removeErrorHeaders(message);
        incrementRetriesCounter(message);
        rabbitTemplate.send(NAMELESS_EXCHANGE, originalQueue, message);
        LOGGER.info("The message has been retried");
    }

    private void incrementRetriesCounter(Message message) {
        Integer retries = getRetries(message).orElse(0);
        message.getMessageProperties().setHeader(X_RETRIES, ++retries);
    }

    private Optional<Integer> getRetries(Message message) {
        return Optional.ofNullable(message.getMessageProperties().getHeader(X_RETRIES));
    }

    private Optional<String> getOriginalQueue(Message message) {
        return Optional.ofNullable(message.getMessageProperties().getHeader(X_ORIGINAL_QUEUE));
    }

    private void removeErrorHeaders(Message message) {
        message.getMessageProperties().getHeaders().remove(X_EXCEPTION_CAUSE);
        message.getMessageProperties().getHeaders().remove(X_EXCEPTION_MESSAGE);
        message.getMessageProperties().getHeaders().remove(X_EXCEPTION_STACKTRACE);
        message.getMessageProperties().getHeaders().remove(X_ORIGINAL_EXCHANGE);
        message.getMessageProperties().getHeaders().remove(X_ORIGINAL_QUEUE);
        message.getMessageProperties().getHeaders().remove(X_ORIGINAL_ROUTING_KEY);
    }
}

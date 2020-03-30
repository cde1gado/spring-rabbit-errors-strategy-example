package com.examples.example2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static com.examples.common.ErrorHeader.*;

public class RetryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryService.class);

    private static final String X_RETRIES = "x-retries";

    private static final String NAMELESS_EXCHANGE = "";

    private RetryProperties retryProperties;

    private RabbitTemplate rabbitTemplate;

    private String parkingLotQueue;

    public RetryService(RetryProperties retryProperties, RabbitTemplate rabbitTemplate, String parkingLotQueue) {
        this.retryProperties = retryProperties;
        this.rabbitTemplate = rabbitTemplate;
        this.parkingLotQueue = parkingLotQueue;
    }

    public void process(Message message) {
        LOGGER.info("Processing failed message");
        Integer retries = getRetries(message);
        if (retries < retryProperties.getDefaultMaxAttempts()) wait(message);
        else discard(message);
    }

    public void retry(Message message) {
        LOGGER.info("Retrying message");
        getOriginalQueue(message).ifPresent(originalQueue -> retry(message, originalQueue));
    }

    private void retry(Message message, String originalQueue) {
        removeErrorHeaders(message);
        incrementRetriesCounter(message);
        rabbitTemplate.send(NAMELESS_EXCHANGE, originalQueue, message);
        LOGGER.info("The message has been retried");
    }

    private void wait(Message message) {
        message.getMessageProperties().setExpiration(retryProperties.getDefaultWaitTime());
        rabbitTemplate.send(retryProperties.getDefaultWaitQueue(), message);
        LOGGER.info("Waiting...");
    }

    private void discard(Message message) {
        rabbitTemplate.send(parkingLotQueue, message);
        LOGGER.info("The message has been discarded");
    }

    private void incrementRetriesCounter(Message message) {
        Integer retries = getRetries(message);
        message.getMessageProperties().setHeader(X_RETRIES, ++retries);
    }

    private Integer getRetries(Message message) {
        return Optional
                .ofNullable((Integer) message.getMessageProperties().getHeader(X_RETRIES))
                .orElse(0);
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

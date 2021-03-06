package io.github.cde1gado.example2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static io.github.cde1gado.common.ErrorHeader.*;

public class RetryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryService.class);

    private static final String NAMELESS_EXCHANGE = "";

    private RetryQueueProperties properties;

    private RetryPolicy policy;

    private BackOffPolicy backOff;

    private RabbitTemplate rabbitTemplate;

    private String parkingLotQueue;

    public RetryService(RetryQueueProperties properties,
                        RetryPolicy policy,
                        BackOffPolicy backOff,
                        RabbitTemplate rabbitTemplate,
                        String parkingLotQueue) {
        this.properties = properties;
        this.policy = policy;
        this.backOff = backOff;
        this.rabbitTemplate = rabbitTemplate;
        this.parkingLotQueue = parkingLotQueue;
    }

    public void process(Message message) {
        LOGGER.info("Processing failed message");
        if (policy.isRetryable(message)) wait(message);
        else discard(message);
    }

    public void retry(Message message) {
        LOGGER.info("Retrying message");
        getOriginalQueue(message).ifPresent(originalQueue -> retry(message, originalQueue));
    }

    private void wait(Message message) {
        backOff.setWaitingTime(message);
        rabbitTemplate.send(properties.getWait(), message);
        LOGGER.info("Waiting...");
    }

    private void discard(Message message) {
        rabbitTemplate.send(parkingLotQueue, message);
        LOGGER.info("The message has been discarded");
    }

    private Optional<String> getOriginalQueue(Message message) {
        return Optional.ofNullable(message.getMessageProperties().getHeader(X_ORIGINAL_QUEUE));
    }

    private void retry(Message message, String originalQueue) {
        removePreviousErrorHeaders(message);
        MessageUtils.incrementRetriesCounter(message);
        rabbitTemplate.send(NAMELESS_EXCHANGE, originalQueue, message);
        LOGGER.info("The message has been retried");
    }

    private void removePreviousErrorHeaders(Message message) {
        message.getMessageProperties().getHeaders().remove(X_EXCEPTION_CAUSE);
        message.getMessageProperties().getHeaders().remove(X_EXCEPTION_MESSAGE);
        message.getMessageProperties().getHeaders().remove(X_EXCEPTION_STACKTRACE);
        message.getMessageProperties().getHeaders().remove(X_ORIGINAL_EXCHANGE);
        message.getMessageProperties().getHeaders().remove(X_ORIGINAL_QUEUE);
        message.getMessageProperties().getHeaders().remove(X_ORIGINAL_ROUTING_KEY);
    }
}

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

public class RetryManagerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryManagerListener.class);

    private static final String X_RETRIES = "x-retries";

    private RabbitTemplate rabbitTemplate;

    private Integer maxAttemps;

    private String waitTime;

    private String waitQueue;

    private String parkingLotQueue;

    public RetryManagerListener(RabbitTemplate rabbitTemplate, RetryProperties properties, String parkingLotQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.maxAttemps = properties.getDefaultMaxAttempts();
        this.waitTime = properties.getDefaultWaitTime();
        this.waitQueue = properties.getDefaultWaitQueue();
        this.parkingLotQueue = parkingLotQueue;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${rabbit.retry.manager.queue}", durable = "true"),
                    exchange = @Exchange(value = "${rabbit.exchange.errors}"),
                    key = "${rabbit.retry.manager.queue}"
            ),
            containerFactory = "retryContainerFactory"
    )
    public void onMessage(Message message) {
        LOGGER.info("Processing failed message");
        Integer retries = getRetries(message).orElse(0);
        if (retries < maxAttemps) wait(message);
        else discard(message);
    }

    private Optional<Integer> getRetries(Message message) {
        return Optional.ofNullable(message.getMessageProperties().getHeader(X_RETRIES));
    }

    private void wait(Message message) {
        message.getMessageProperties().setExpiration(waitTime);
        rabbitTemplate.send(waitQueue, message);
        LOGGER.info("Waiting...");
    }

    private void discard(Message message) {
        rabbitTemplate.send(parkingLotQueue, message);
        LOGGER.info("The message has been discarded");
    }
}

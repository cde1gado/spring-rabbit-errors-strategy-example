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

public class RetrySenderListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrySenderListener.class);

    private static final String X_ORIGINAL_QUEUE = "x-original-queue";

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
        rabbitTemplate.send(NAMELESS_EXCHANGE, originalQueue, message);
        LOGGER.info("The message has been retried");
    }

    private Optional<String> getOriginalQueue(Message message) {
        return Optional.ofNullable(message.getMessageProperties().getHeader(X_ORIGINAL_QUEUE));
    }

    private void removeErrorHeaders(Message message) {
        message.getMessageProperties().getHeaders().remove("x-exception-cause");
        message.getMessageProperties().getHeaders().remove("x-exception-message");
        message.getMessageProperties().getHeaders().remove("x-exception-stacktrace");
        message.getMessageProperties().getHeaders().remove("x-original-exchange");
        message.getMessageProperties().getHeaders().remove("x-original-queue");
        message.getMessageProperties().getHeaders().remove("x-original-routingKey");
    }
}

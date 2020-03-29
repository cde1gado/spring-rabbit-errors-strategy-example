package com.examples.example2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RetryManagerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryManagerListener.class);

    private RabbitTemplate rabbitTemplate;

    private String waitTime;

    private String waitQueue;

    public RetryManagerListener(RabbitTemplate rabbitTemplate, String waitTime, String waitQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.waitTime = waitTime;
        this.waitQueue = waitQueue;
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
        wait(message);
    }

    private void wait(Message message) {
        message.getMessageProperties().setExpiration(waitTime);
        rabbitTemplate.send(waitQueue, message);
        LOGGER.info("Waiting...");
    }
}

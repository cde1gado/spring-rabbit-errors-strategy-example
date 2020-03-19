package com.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import static com.examples.RabbitConfiguration.PRIMARY_QUEUE;

public class PrimaryListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryListener.class);

    @RabbitListener(queues = PRIMARY_QUEUE, containerFactory = "containerFactory")
    public void onMessage(Message message) {
        LOGGER.info("Processing message {}", message);
        throw new RuntimeException("There was an error");
    }
}

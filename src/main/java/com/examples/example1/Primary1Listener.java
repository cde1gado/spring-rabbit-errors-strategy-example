package com.examples.example1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class Primary1Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Primary1Listener.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${rabbit.primary.queue1}", durable = "true"),
                    exchange = @Exchange(value = "${rabbit.exchange.events}"),
                    key = "${rabbit.primary.routing-key}"
            ),
            containerFactory = "containerFactory"
    )
    public void onMessage(Message message) {
        LOGGER.info("Processing message {}", message);
        throw new RuntimeException("There was an error");
    }
}

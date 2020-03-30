package com.examples.example2;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class RetryListener {

    private RetryService retryService;

    public RetryListener(RetryService retryService) {
        this.retryService = retryService;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${rabbit.retry.manager.queue}", durable = "true"),
                    exchange = @Exchange(value = "${rabbit.exchange.errors}"),
                    key = "${rabbit.retry.manager.queue}"
            ),
            containerFactory = "retryContainerFactory"
    )
    public void onFailedMessage(Message message) {
        retryService.process(message);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${rabbit.retry.sender.queue}", durable = "true"),
                    exchange = @Exchange(value = "${rabbit.exchange.errors}"),
                    key = "${rabbit.retry.sender.queue}"
            ),
            containerFactory = "retryContainerFactory"
    )
    public void onRetryableMessage(Message message) {
        retryService.retry(message);
    }
}

package io.github.cde1gado.example2;

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
                    value = @Queue(value = "${rabbit.retry.queue.manager}", durable = "true"),
                    exchange = @Exchange(value = "${rabbit.exchange.errors}"),
                    key = "${rabbit.retry.queue.manager}"
            ),
            containerFactory = "retryContainerFactory"
    )
    public void onFailedMessage(Message message) {
        retryService.process(message);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${rabbit.retry.queue.sender}", durable = "true"),
                    exchange = @Exchange(value = "${rabbit.exchange.errors}"),
                    key = "${rabbit.retry.queue.sender}"
            ),
            containerFactory = "retryContainerFactory"
    )
    public void onRetryableMessage(Message message) {
        retryService.retry(message);
    }
}

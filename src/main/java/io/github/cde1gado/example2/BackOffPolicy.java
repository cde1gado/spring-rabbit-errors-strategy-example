package io.github.cde1gado.example2;

import org.springframework.amqp.core.Message;

public class BackOffPolicy {

    private Long init;

    private Long max;

    private Double multiplier;

    public BackOffPolicy(BackOffProperties properties) {
        this.init = properties.getInit();
        this.max = properties.getMax();
        this.multiplier = properties.getMultiplier();
    }

    public void setWaitingTime(Message message) {
        Long waitingTime = MessageUtils.getWaitingTime(message)
                .map(previousWaitingTime -> getNextWaitingTime(message, previousWaitingTime))
                .orElse(init);
        MessageUtils.setWaitingTime(message, waitingTime);
    }

    private Long getNextWaitingTime(Message message, Long previousWaitingTime) {
        return Math.min((long) (previousWaitingTime * multiplier), max);
    }
}

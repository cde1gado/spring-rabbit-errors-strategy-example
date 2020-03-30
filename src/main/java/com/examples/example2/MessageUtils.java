package com.examples.example2;

import org.springframework.amqp.core.Message;

import java.util.Optional;

public class MessageUtils {

    private static final String X_RETRIES = "x-retries";

    private static final String X_WAITING_TIME = "x-waiting-time";

    public static Integer getRetriesCounter(Message message) {
        return Optional.ofNullable((Integer) message.getMessageProperties().getHeader(X_RETRIES)).orElse(0);
    }

    public static void incrementRetriesCounter(Message message) {
        Integer retries = getRetriesCounter(message);
        message.getMessageProperties().setHeader(X_RETRIES, ++retries);
    }

    public static Optional<Long> getWaitingTime(Message message) {
        return Optional.ofNullable(message.getMessageProperties().getHeader(X_WAITING_TIME));
    }

    public static void setWaitingTime(Message message, Long waitingTime) {
        message.getMessageProperties().setHeader(X_WAITING_TIME, waitingTime);
        message.getMessageProperties().setExpiration(String.valueOf(waitingTime));
    }
}

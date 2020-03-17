package com.example;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    private static final String EVENTS_EXCHANGE = "events";

    public static final String PRIMARY_QUEUE = "example.primary.message";

    private static final String PRIMARY_ROUTING_KEY = "primary.message";

    @Bean
    DirectExchange eventsExchange() {
        return new DirectExchange(EVENTS_EXCHANGE);
    }

    @Bean
    Queue primaryQueue() {
        return QueueBuilder.durable(PRIMARY_QUEUE).build();
    }

    @Bean
    Binding primaryBinding(Queue primaryQueue, DirectExchange eventsExchange) {
        return BindingBuilder.bind(primaryQueue).to(eventsExchange).with(PRIMARY_ROUTING_KEY);
    }

    @Bean
    PrimaryListener primaryListener() {
        return new PrimaryListener();
    }
}

package io.github.cde1gado.common;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Value("${rabbit.exchange.errors}")
    private String errorsEx;

    @Value("${rabbit.parking-lot.queue}")
    private String parkingLotQ;

    @Bean
    DirectExchange errorsExchange() {
        return new DirectExchange(errorsEx);
    }

    @Bean
    Queue parkingLotQueue() {
        return QueueBuilder.durable(parkingLotQ).build();
    }

    @Bean
    Binding parkingLotBinding(Queue parkingLotQueue, DirectExchange errorsExchange) {
        return BindingBuilder.bind(parkingLotQueue).to(errorsExchange).with(parkingLotQ);
    }
}

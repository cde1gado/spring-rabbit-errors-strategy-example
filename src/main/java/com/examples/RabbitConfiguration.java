package com.examples;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitConfiguration {

    private static final String EVENTS_EXCHANGE = "events";

    private static final String ERRORS_EXCHANGE = "errors";

    public static final String PRIMARY_QUEUE = "example.primary.message";

    private static final String PARKING_LOT_QUEUE = "parking-lot";

    private static final String PRIMARY_ROUTING_KEY = "primary.message";

    @Value("${default-requeue-rejected}")
    private boolean defaultQueueRejected;

    @Bean
    DirectExchange eventsExchange() {
        return new DirectExchange(EVENTS_EXCHANGE);
    }

    @Bean
    DirectExchange errorsExchange() {
        return new DirectExchange(ERRORS_EXCHANGE);
    }

    @Bean
    Queue primaryQueue() {
        return QueueBuilder.durable(PRIMARY_QUEUE).build();
    }

    @Bean
    Queue parkingLotQueue() {
        return QueueBuilder.durable(PARKING_LOT_QUEUE).build();
    }

    @Bean
    Binding primaryBinding(Queue primaryQueue, DirectExchange eventsExchange) {
        return BindingBuilder.bind(primaryQueue).to(eventsExchange).with(PRIMARY_ROUTING_KEY);
    }

    @Bean
    Binding parkingLotBinding(Queue parkingLotQueue, DirectExchange errorsExchange) {
        return BindingBuilder.bind(parkingLotQueue).to(errorsExchange).with(PARKING_LOT_QUEUE);
    }

    @Bean
    PrimaryListener primaryListener() {
        return new PrimaryListener();
    }

    @Bean
    MessageRecoverer errorRecoverer(RabbitTemplate rabbitTemplate) {
        return new ErrorRecoverer(rabbitTemplate, ERRORS_EXCHANGE, PARKING_LOT_QUEUE);
    }

    @Bean
    RetryOperationsInterceptor errorRecovererInterceptor(MessageRecoverer errorRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(1) // no retry, because it's blocking
                .recoverer(errorRecoverer)
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory containerFactory(ConnectionFactory connectionFactory,
                                                                 RetryOperationsInterceptor errorRecovererInterceptor) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setAdviceChain(errorRecovererInterceptor);
        containerFactory.setDefaultRequeueRejected(defaultQueueRejected);
        return containerFactory;
    }
}

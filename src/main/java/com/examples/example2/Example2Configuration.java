package com.examples.example2;

import com.examples.common.ErrorRecoverer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
@EnableConfigurationProperties({RetryQueueProperties.class, PolicyProperties.class, BackOffProperties.class})
public class Example2Configuration {

    @Value("${rabbit.exchange.errors}")
    private String errorsEx;

    @Value("${rabbit.parking-lot.queue}")
    private String parkingLotQ;

    @Value("${rabbit.default-requeue-rejected}")
    private boolean defaultRequeueRejected;

    @Bean
    Queue waitQueue(RetryQueueProperties properties) {
        return QueueBuilder
                .durable(properties.getWait())
                .deadLetterExchange(errorsEx)
                .deadLetterRoutingKey(properties.getSender())
                .build();
    }

    @Bean
    Binding waitBinding(Queue waitQueue, DirectExchange errorsExchange, RetryQueueProperties properties) {
        return BindingBuilder.bind(waitQueue).to(errorsExchange).with(properties.getWait());
    }

    @Bean
    Primary2Listener primary2Listener() {
        return new Primary2Listener();
    }

    @Bean
    RetryListener retryListener(RetryService retryService) {
        return new RetryListener(retryService);
    }

    @Bean
    RetryPolicy policy(PolicyProperties properties) {
        return new RetryPolicy(properties);
    }

    @Bean
    BackOffPolicy backOff(BackOffProperties properties) {
        return new BackOffPolicy(properties);
    }

    @Bean
    RetryService retryService(RetryQueueProperties properties,
                              RetryPolicy policy,
                              BackOffPolicy backoff,
                              RabbitTemplate rabbitTemplate) {
        return new RetryService(properties, policy, backoff, rabbitTemplate, parkingLotQ);
    }

    @Bean
    MessageRecoverer retryRecoverer(RabbitTemplate rabbitTemplate, RetryQueueProperties properties) {
        return new ErrorRecoverer(rabbitTemplate, errorsEx, properties.getManager());
    }

    @Bean
    RetryOperationsInterceptor retryRecovererInterceptor(MessageRecoverer retryRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(1) // no retry, because it's blocking
                .recoverer(retryRecoverer)
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory retryContainerFactory(ConnectionFactory connectionFactory,
                                                                      RetryOperationsInterceptor retryRecovererInterceptor) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(connectionFactory);
        containerFactory.setAdviceChain(retryRecovererInterceptor);
        containerFactory.setDefaultRequeueRejected(defaultRequeueRejected);
        return containerFactory;
    }
}

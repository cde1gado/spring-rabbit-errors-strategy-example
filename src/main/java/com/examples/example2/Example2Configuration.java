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
@EnableConfigurationProperties(RetryProperties.class)
public class Example2Configuration {

    @Value("${rabbit.exchange.errors}")
    private String errorsEx;

    @Value("${rabbit.retry.manager.queue}")
    private String retryManagerQ;

    @Value("${rabbit.retry.sender.queue}")
    private String retrySenderQ;

    @Value("${rabbit.retry.default-wait-queue}")
    private String waitQ;

    @Value("${rabbit.parking-lot.queue}")
    private String parkingLotQ;

    @Value("${rabbit.default-requeue-rejected}")
    private boolean defaultRequeueRejected;

    @Bean
    Queue waitQueue() {
        return QueueBuilder
                .durable(waitQ)
                .deadLetterExchange(errorsEx)
                .deadLetterRoutingKey(retrySenderQ)
                .build();
    }

    @Bean
    Binding waitBinding(Queue waitQueue, DirectExchange errorsExchange) {
        return BindingBuilder.bind(waitQueue).to(errorsExchange).with(waitQ);
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
    RetryService retryService(RetryProperties retryProperties, RabbitTemplate rabbitTemplate) {
        return new RetryService(retryProperties, rabbitTemplate, parkingLotQ);
    }

    @Bean
    MessageRecoverer retryRecoverer(RabbitTemplate rabbitTemplate) {
        return new ErrorRecoverer(rabbitTemplate, errorsEx, retryManagerQ);
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

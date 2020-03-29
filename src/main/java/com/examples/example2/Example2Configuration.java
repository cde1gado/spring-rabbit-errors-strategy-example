package com.examples.example2;

import com.examples.common.ErrorRecoverer;
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
public class Example2Configuration {

    @Value("${rabbit.exchange.errors}")
    private String errorsEx;

    @Value("${rabbit.retry.manager.queue}")
    private String retryManagerQ;

    @Value("${rabbit.retry.sender.queue}")
    private String retrySenderQ;

    @Value("${rabbit.retry.wait.queue}")
    private String waitQ;

    @Value("${rabbit.retry.default-wait-time}")
    private String defaultWaitTime; // millis

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
    MessageRecoverer retryRecoverer(RabbitTemplate rabbitTemplate) {
        return new ErrorRecoverer(rabbitTemplate, errorsEx, retryManagerQ);
    }

    @Bean
    Primary2Listener primary2Listener() {
        return new Primary2Listener();
    }

    @Bean
    RetryManagerListener retryManagerListener(RabbitTemplate rabbitTemplate) {
        return new RetryManagerListener(rabbitTemplate, defaultWaitTime, waitQ);
    }

    @Bean
    RetrySenderListener retrySenderListener(RabbitTemplate rabbitTemplate) {
        return new RetrySenderListener(rabbitTemplate);
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

package com.examples.example1;

import com.examples.common.ErrorRecoverer;
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
public class Example1Configuration {

    @Value("${rabbit.exchange.errors}")
    private String errorsEx;

    @Value("${rabbit.parking-lot.queue}")
    private String parkingLotQ;

    @Value("${rabbit.default-requeue-rejected}")
    private boolean defaultQueueRejected;

    @Bean
    Primary1Listener primary1Listener() {
        return new Primary1Listener();
    }

    @Bean
    MessageRecoverer errorRecoverer(RabbitTemplate rabbitTemplate) {
        return new ErrorRecoverer(rabbitTemplate, errorsEx, parkingLotQ);
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

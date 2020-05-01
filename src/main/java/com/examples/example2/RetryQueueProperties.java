package com.examples.example2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbit.retry.queue")
public class RetryQueueProperties {

    @Getter @Setter private String manager;

    @Getter @Setter private String sender;

    @Getter @Setter private String wait;
}

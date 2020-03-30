package com.examples.example2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbit.retry")
public class RetryProperties {

    @Getter @Setter private String managerQueue;

    @Getter @Setter private String senderQueue;

    @Getter @Setter private String waitQueue;
}

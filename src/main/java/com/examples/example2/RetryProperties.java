package com.examples.example2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbit.retry")
public class RetryProperties {

    @Getter @Setter private String defaultWaitQueue;

    @Getter @Setter private String defaultWaitTime;

    @Getter @Setter private Integer defaultMaxAttempts;
}

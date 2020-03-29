package com.examples.example2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbit.retry")
public class RetryProperties {

    private String defaultWaitQueue;

    private String defaultWaitTime;

    private Integer defaultMaxAttempts;

    public String getDefaultWaitQueue() {
        return defaultWaitQueue;
    }

    public void setDefaultWaitQueue(String defaultWaitQueue) {
        this.defaultWaitQueue = defaultWaitQueue;
    }

    public String getDefaultWaitTime() {
        return defaultWaitTime;
    }

    public void setDefaultWaitTime(String defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime;
    }

    public Integer getDefaultMaxAttempts() {
        return defaultMaxAttempts;
    }

    public void setDefaultMaxAttempts(Integer defaultMaxAttempts) {
        this.defaultMaxAttempts = defaultMaxAttempts;
    }
}

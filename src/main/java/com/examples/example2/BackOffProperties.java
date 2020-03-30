package com.examples.example2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbit.retry.back-off")
public class BackOffProperties {

    @Getter @Setter private Long init; // millis

    @Getter @Setter private Long max; // millis

    @Getter @Setter private Double multiplier;
}

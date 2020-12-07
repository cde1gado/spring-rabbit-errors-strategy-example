package io.github.cde1gado.example2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "rabbit.retry.policy")
public class PolicyProperties {

    @Getter @Setter private Integer maxAttempts;

    @Getter @Setter private List<String> errorTypes;
}

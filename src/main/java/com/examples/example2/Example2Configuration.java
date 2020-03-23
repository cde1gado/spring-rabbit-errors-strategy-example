package com.examples.example2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Example2Configuration {

    @Bean
    Primary2Listener primary2Listener() {
        return new Primary2Listener();
    }
}

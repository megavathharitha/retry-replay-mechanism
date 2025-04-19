package com.example.transactionretryreplay.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "retry.jobs")
@Getter
@Setter
public class JobConfig {
    private Map<String, JobDefinition> definitions;

    public Map<String, JobDefinition> getDefinitions() {
        return definitions;
    }

    @Getter
    @Setter
    public static class JobDefinition {
        private String targetBeanName;
        private String targetMethodName;
        private String retryStrategy;
        private long fixedInterval;
        private long exponentialInitialInterval;
        private double exponentialMultiplier;
        private int circuitBreakerFailureThreshold;
        private long circuitBreakerResetTimeout;
        private int circuitBreakerHalfOpenAttempts;
        private long jitterFactor;
        private int maxAttempts;


    }
}
package com.example.transactionretryreplay.notifications;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification.email")
@Getter
@Setter
public class EmailTemplateConfig {
    private String defaultFrom;
    // You can add more configuration here if needed:
    private String defaultCc;
    private String defaultBcc;
    private String retrySuccessSubject;
    private String retryFailureSubject;
    private String replaySuccessSubject;
    private String replayFailureSubject;
}
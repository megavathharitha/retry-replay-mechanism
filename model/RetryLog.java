package com.example.transactionretryreplay.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RetryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private LocalDateTime attemptedAt;
    private int attemptNumber;
    private String retryStrategyUsed;
    private long delayMillis;
    private String status; // e.g., "SUCCESS", "FAILED"
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
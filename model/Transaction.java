package com.example.transactionretryreplay.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId; // Unique identifier for the transaction
    private String transactionType;
    private String status; // e.g., "PENDING", "SUCCESS", "FAILED"
    @Column(columnDefinition = "TEXT")
    private String payload; // Transaction data (e.g., as JSON)
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // Add other relevant fields as needed
}
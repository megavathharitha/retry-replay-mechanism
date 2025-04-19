package com.example.transactionretryreplay.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ReplayLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status; // e.g., "SUCCESS", "FAILED"
    private String replayedBy; // User who initiated the replay
    private String replayType; // e.g., "MANUAL", "SCHEDULED"
    // Add fields for replay parameters if needed (scope, timing etc.)
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
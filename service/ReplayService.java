package com.example.transactionretryreplay.service;


import com.example.transactionretryreplay.model.ReplayLog;
import com.example.transactionretryreplay.model.Transaction;
import com.example.transactionretryreplay.notifications.EmailService;
import com.example.transactionretryreplay.notifications.EmailTemplateConfig;
import com.example.transactionretryreplay.repository.ReplayLogRepository;
import com.example.transactionretryreplay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplayService {

    private final TransactionRepository transactionRepository;
    private final ReplayLogRepository replayLogRepository;
    private final EmailTemplateConfig emailTemplateConfig;
    private final ApplicationContext applicationContext;

    // Assume you have other services that handle the actual transaction processing
    // For example:
     private final OrderProcessingService orderProcessingService;
     private final InventoryService inventoryService;

    public List<Transaction> getFailedTransactions() {
        return transactionRepository.findByStatus("FAILED");
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional
    public ReplayLog replayTransaction(Long transactionId, String replayedBy) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
        if (optionalTransaction.isEmpty()) {
            log.warn("Transaction with ID {} not found for replay.", transactionId);
            return null;
        }
        Transaction transaction = optionalTransaction.get();

        ReplayLog replayLog = new ReplayLog();
        replayLog.setTransaction(transaction);
        replayLog.setRequestedAt(LocalDateTime.now());
        replayLog.setReplayedBy(replayedBy);
        replayLog.setStatus("PENDING");
        replayLogRepository.save(replayLog);

        try {
            // **Crucial Part: Implement the actual replay logic based on transaction type**
            switch (transaction.getTransactionType()) {
                case "ORDER":
                    log.error("OrderProcessingService not available for replaying order transaction {}", transactionId);
                    replayLog.setStatus("FAILED");
                    replayLog.setErrorMessage("OrderProcessingService not available.");
                    break;
                case "INVENTORY_UPDATE":
                    log.error("InventoryService not available for replaying inventory transaction {}", transactionId);
                    replayLog.setStatus("FAILED");
                    replayLog.setErrorMessage("InventoryService not available.");
                    break;
                // Add cases for other transaction types
                default:
                    log.warn("No specific replay logic defined for transaction type: {}", transaction.getTransactionType());
                    replayLog.setStatus("SUCCESS"); // Or handle differently
                    break;
            }

            if ("PENDING".equals(replayLog.getStatus())) {
                transaction.setStatus("SUCCESS"); // Update transaction status on successful replay
                transaction.setLastUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                replayLog.setStatus("SUCCESS");
                log.info("Transaction {} replayed successfully.", transactionId);
            }

        } catch (Exception e) {
            log.error("Error replaying transaction {}: {}", transactionId, e.getMessage());
            replayLog.setStatus("FAILED");
            replayLog.setErrorMessage(e.getMessage());
            transaction.setStatus("FAILED_REPLAY"); // Or a specific replay failure status
            transaction.setLastUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        } finally {
            replayLogRepository.save(replayLog);

            Map<String, Object> model = new HashMap<>();
            model.put("transactionId", transactionId);
            model.put("success", "SUCCESS".equals(replayLog.getStatus()));
            model.put("errorMessage", replayLog.getErrorMessage());
            model.put("replayedBy", replayedBy);
            applicationContext.getBean(EmailService.class).sendEmail(
                    "user@example.com", // Configure recipient properly
                    "SUCCESS".equals(replayLog.getStatus()) ?
                            emailTemplateConfig.getReplaySuccessSubject() :
                            emailTemplateConfig.getReplayFailureSubject(),
                    "replay-completion.html",
                    model
            );

            if ("FAILED".equals(replayLog.getStatus())) {
                log.error("Incident: Replay failed for transaction {}", transactionId);
            }
        }

        return replayLog;
    }


    public ReplayLog replayTransactionGeneric(Long transactionId, String replayedBy, Map<String, Object> context) {

        return null;
    }


    public List<Transaction> getTransactionsByScope(String scope) {
        return transactionRepository.findAll(); // Placeholder
    }

    public List<Transaction> getTransactionsByTiming(LocalDateTime startTime, LocalDateTime endTime) {

        return transactionRepository.findAll(); // Placeholder
    }

    // Method to schedule replay for a later time (using Quartz?)
    public void scheduleReplay(Long transactionId, LocalDateTime replayTime, String replayedBy) {
        log.info("Scheduled replay for transaction {} at {}", transactionId, replayTime);

    }
}
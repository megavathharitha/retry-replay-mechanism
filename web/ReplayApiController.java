package com.example.transactionretryreplay.web;


import com.example.transactionretryreplay.model.ReplayLog;
import com.example.transactionretryreplay.model.Transaction;
import com.example.transactionretryreplay.repository.ReplayLogRepository;
import com.example.transactionretryreplay.service.ReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/replay") // Use the /api prefix for your API
public class ReplayApiController {

    private final ReplayService replayService;
    private final ReplayLogRepository replayLogRepository;

    @GetMapping("/failed")
    public ResponseEntity<List<Transaction>> getFailedTransactionsJson() {
        List<Transaction> failedTransactions = replayService.getFailedTransactions();
        return ResponseEntity.ok(failedTransactions);
    }

    @PostMapping("/trigger/{transactionId}")
    public ResponseEntity<String> triggerReplayJson(@PathVariable Long transactionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String replayedBy = authentication.getName(); // Get the username of the logged-in user

        log.info("Replay triggered for transaction ID: {} by user: {}", transactionId, replayedBy);
        replayService.replayTransaction(transactionId, replayedBy);
        return ResponseEntity.ok("Replay triggered successfully for transaction ID: " + transactionId);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReplayLog>> getReplayHistoryJson() {
        List<ReplayLog> replayLogs = replayLogRepository.findAll();
        return ResponseEntity.ok(replayLogs);
    }
}
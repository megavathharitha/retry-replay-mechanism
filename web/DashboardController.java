package com.example.transactionretryreplay.web;


import com.example.transactionretryreplay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TransactionRepository transactionRepository;
    private final SchedulerFactoryBean schedulerFactoryBean; // Inject SchedulerFactoryBean

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDashboardStatus() {
        Map<String, Object> status = new HashMap<>();
        long totalTransactions = transactionRepository.count();
        long failedTransactions = transactionRepository.findByStatus("FAILED").size();
        // Add logic to count successful, pending, etc., if needed
        status.put("totalTransactions", totalTransactions);
        status.put("failedTransactions", failedTransactions);

        try {
            status.put("scheduledJobsCount", schedulerFactoryBean.getScheduler().getJobKeys(org.quartz.impl.matchers.GroupMatcher.anyJobGroup()).size());
        } catch (SchedulerException e) {
            log.error("Error retrieving scheduled job count: {}", e.getMessage());
            status.put("scheduledJobsCount", -1); // Indicate an error
        }

        // Add more relevant metrics as needed:
        // - Number of successful retries
        // - Number of failed retries (max attempts reached)
        // - Number of pending replays
        // - Recent transaction activity

        return ResponseEntity.ok(status);
    }
}
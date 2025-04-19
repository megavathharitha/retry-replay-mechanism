package com.example.transactionretryreplay.service;


import com.example.transactionretryreplay.job.RetryableJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("orderProcessingService")
@Slf4j
public class OrderProcessingService implements RetryableJob {

    public void processOrder() throws Exception {
        log.info("Attempting to process order...");
        // Simulate a failure on some attempts
        if (Math.random() < 0.7) {
            log.error("Order processing failed!");
            throw new Exception("Order processing failed");
        }
        log.info("Order processed successfully!");
    }

    @Override
    public void execute() throws Exception {
        processOrder();
    }
}
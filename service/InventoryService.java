package com.example.transactionretryreplay.service;


import com.example.transactionretryreplay.job.RetryableJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("inventoryService")
@Slf4j
public class InventoryService implements RetryableJob {

    public void updateInventory() throws Exception {
        log.info("Attempting to update inventory...");
        if (Math.random() < 0.5) {
            log.warn("Inventory update temporarily unavailable.");
            throw new RuntimeException("Inventory service unavailable");
        }
        log.info("Inventory updated successfully.");
    }

    @Override
    public void execute() throws Exception {
        updateInventory();
    }
}
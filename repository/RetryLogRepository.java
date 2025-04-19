package com.example.transactionretryreplay.repository;


import com.example.transactionretryreplay.model.RetryLog;
import com.example.transactionretryreplay.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetryLogRepository extends JpaRepository<RetryLog, Long> {
    List<RetryLog> findByTransaction(Transaction transaction);
}
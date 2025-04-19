package com.example.transactionretryreplay.repository;


import com.example.transactionretryreplay.model.ReplayLog;
import com.example.transactionretryreplay.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplayLogRepository extends JpaRepository<ReplayLog, Long> {
    List<ReplayLog> findByTransaction(Transaction transaction);
}
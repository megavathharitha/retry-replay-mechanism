package com.example.transactionretryreplay.repository;


import com.example.transactionretryreplay.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByStatus(String status);
    // Add other query methods as needed (e.g., by transactionType, date range)
}
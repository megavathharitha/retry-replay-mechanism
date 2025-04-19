package com.example.transactionretryreplay.retry;

public interface RetryStrategy {

    long getNextDelay(int attempt);
}

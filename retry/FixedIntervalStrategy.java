package com.example.transactionretryreplay.retry;


public class FixedIntervalStrategy implements RetryStrategy {
    private final long intervalMillis;

    public FixedIntervalStrategy(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    @Override
    public long getNextDelay(int attempt) {
        return intervalMillis;
    }
}
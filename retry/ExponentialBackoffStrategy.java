package com.example.transactionretryreplay.retry;

public class ExponentialBackoffStrategy implements RetryStrategy {
    private final long initialIntervalMillis;
    private final double multiplier;

    public ExponentialBackoffStrategy(long initialIntervalMillis, double multiplier) {
        this.initialIntervalMillis = initialIntervalMillis;
        this.multiplier = multiplier;
    }

    @Override
    public long getNextDelay(int attempt) {
        return (long) (initialIntervalMillis * Math.pow(multiplier, attempt - 1));
    }
}
package com.example.transactionretryreplay.retry;


import java.util.Random;

public class JitterStrategy implements RetryStrategy {
    private final RetryStrategy delegate;
    private final long jitterFactorMillis;
    private final Random random = new Random();

    public JitterStrategy(RetryStrategy delegate, long jitterFactorMillis) {
        this.delegate = delegate;
        this.jitterFactorMillis = jitterFactorMillis;
    }

    @Override
    public long getNextDelay(int attempt) {
        long baseDelay = delegate.getNextDelay(attempt);
        long jitter = random.nextLong(2 * jitterFactorMillis + 1) - jitterFactorMillis;
        return Math.max(0, baseDelay + jitter);
    }
}
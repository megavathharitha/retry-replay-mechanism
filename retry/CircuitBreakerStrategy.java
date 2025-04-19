package com.example.transactionretryreplay.retry;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CircuitBreakerStrategy implements RetryStrategy {
    private final int failureThreshold;
    private final long resetTimeoutMillis;
    private final int halfOpenAttempts;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicInteger halfOpenAttemptCount = new AtomicInteger(0);
    private volatile State state = State.CLOSED;

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    public CircuitBreakerStrategy(int failureThreshold, long resetTimeoutMillis, int halfOpenAttempts) {
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMillis = resetTimeoutMillis;
        this.halfOpenAttempts = halfOpenAttempts;
    }

    public void recordFailure() {
        if (state == State.CLOSED) {
            if (failureCount.incrementAndGet() >= failureThreshold) {
                state = State.OPEN;
                lastFailureTime.set(System.currentTimeMillis());
                halfOpenAttemptCount.set(0);
            }
        } else if (state == State.HALF_OPEN) {
            state = State.OPEN;
            lastFailureTime.set(System.currentTimeMillis());
            halfOpenAttemptCount.set(0);
        }
    }

    public void recordSuccess() {
        failureCount.set(0);
        state = State.CLOSED;
        halfOpenAttemptCount.set(0);
    }

    @Override
    public long getNextDelay(int attempt) {
        long now = System.currentTimeMillis();
        if (state == State.OPEN && now - lastFailureTime.get() < resetTimeoutMillis) {
            throw new IllegalStateException("Circuit breaker is open");
        } else if (state == State.OPEN) {
            state = State.HALF_OPEN;
            halfOpenAttemptCount.set(1);
            return 0; // Retry immediately in half-open
        } else if (state == State.HALF_OPEN) {
            if (halfOpenAttemptCount.getAndIncrement() > halfOpenAttempts) {
                throw new IllegalStateException("Circuit breaker in half-open state, max attempts reached");
            }
            return 0; // Retry immediately in half-open
        }
        return 0; // Retry immediately in closed state
    }
}
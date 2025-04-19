package com.example.transactionretryreplay.job;

public interface RetryableJob {

    void execute() throws Exception;
}

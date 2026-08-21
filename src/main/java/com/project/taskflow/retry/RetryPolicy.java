package com.project.taskflow.retry;

public class RetryPolicy {

    private final int maxRetries;

    public RetryPolicy(int maxRetries) {

        this.maxRetries = maxRetries;
    }

    public int getMaxRetries() {

        return maxRetries;
    }
}
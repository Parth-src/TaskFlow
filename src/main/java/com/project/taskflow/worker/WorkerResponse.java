package com.project.taskflow.worker;

public class WorkerResponse {

    private boolean success;

    private boolean retry;

    private String message;

    public WorkerResponse() {
    }

    public WorkerResponse(
            boolean success,
            boolean retry,
            String message) {

        this.success = success;
        this.retry = retry;
        this.message = message;
    }

    public boolean isSuccess() {

        return success;
    }

    public boolean shouldRetry() {

        return retry;
    }

    public String getMessage() {

        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

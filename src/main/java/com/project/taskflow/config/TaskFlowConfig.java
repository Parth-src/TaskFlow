package com.project.taskflow.config;

public class TaskFlowConfig {

    private WorkerConfig worker;

    public WorkerConfig getWorker() {
        return worker;
    }

    public void setWorker(
            WorkerConfig worker) {

        this.worker = worker;
    }

    public static class WorkerConfig {

        private String host;

        public String getHost() {
            return host;
        }

        public void setHost(
                String host) {

            this.host = host;
        }
    }
}
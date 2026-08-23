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

        private String token;


        public String getHost() {

            return host;
        }

        public void setHost(
                String host) {

            this.host = host;
        }


        public String getToken() {

            return token;
        }

        public void setToken(
                String token) {

            this.token = token;
        }
    }
}
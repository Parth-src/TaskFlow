package com.project.taskflow.monitoring;

import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

import java.util.List;

public class HttpWorkerMonitorTest {

    public static void main(String[] args) {

        WorkerRegistry registry =
                new WorkerRegistry();

        registry.discover(
                "http://localhost:8081"
        );

        HttpWorkerMonitor monitor =
                new HttpWorkerMonitor(
                        registry
                );

        List<WorkerStatus> workers =
                monitor.getWorkers();

        System.out.println(
                "Workers: "
                        + workers.size()
        );

        for (WorkerStatus worker : workers) {

            System.out.println(
                    "Worker: "
                            + worker.getWorkerId()
            );

            System.out.println(
                    "Endpoint: "
                            + worker.getEndpoint()
            );

            System.out.println(
                    "Status: "
                            + (
                            worker.isHealthy()
                                    ? "ONLINE"
                                    : "OFFLINE"
                    )
            );

            System.out.println(
                    "-------------------------"
            );
        }
    }
}
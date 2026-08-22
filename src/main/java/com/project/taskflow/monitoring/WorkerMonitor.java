package com.project.taskflow.monitoring;

import com.project.taskflow.worker.WorkerMetadata;

import java.util.List;

public interface WorkerMonitor {

    boolean isHealthy(
            WorkerMetadata worker
    );

    List<WorkerStatus> getWorkers();
}
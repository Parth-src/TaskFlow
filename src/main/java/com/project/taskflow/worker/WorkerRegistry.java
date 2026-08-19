package com.project.taskflow.worker;

import java.util.HashMap;
import java.util.Map;

public class WorkerRegistry {

    private final Map<String, WorkerMetadata>
            workers = new HashMap<>();

    public void register(
            String workerId,
            String endpoint) {

        workers.put(
                workerId,
                new WorkerMetadata(
                        workerId,
                        endpoint
                )
        );
    }

    public WorkerMetadata get(
            String workerId) {

        return workers.get(workerId);
    }
}

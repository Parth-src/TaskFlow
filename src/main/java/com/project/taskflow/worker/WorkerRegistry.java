package com.project.taskflow.worker;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    public void discover(
            String baseUrl) {

        WorkerDiscovery discovery =
                new WorkerDiscovery();

        List<String> workerIds =
                discovery.discover(baseUrl);

        for (String workerId :
                workerIds) {

            register(
                    workerId,
                    baseUrl + "/workers/" + workerId
            );
        }
    }

    public Collection<WorkerMetadata> getAll() {
        return workers.values();
    }
}
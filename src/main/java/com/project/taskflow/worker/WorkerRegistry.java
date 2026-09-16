package com.project.taskflow.worker;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerRegistry {

    private static final java.util.regex.Pattern WORKER_ID_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final Map<String, WorkerMetadata>
            workers = new HashMap<>();

    public void register(
            String workerId,
            String endpoint) {

        if (workerId == null || !WORKER_ID_PATTERN.matcher(workerId).matches()) {
            throw new IllegalArgumentException("Invalid worker ID format: " + workerId);
        }

        if (endpoint == null || (!endpoint.startsWith("http://") && !endpoint.startsWith("https://"))) {
            throw new IllegalArgumentException("Invalid worker endpoint scheme (must be http/https): " + endpoint);
        }

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

        if (baseUrl == null || (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://"))) {
            throw new IllegalArgumentException("Invalid worker base URL scheme (must be http/https): " + baseUrl);
        }

        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        WorkerDiscovery discovery =
                new WorkerDiscovery();

        List<String> workerIds =
                discovery.discover(cleanBaseUrl);

        for (String workerId :
                workerIds) {

            if (workerId != null && WORKER_ID_PATTERN.matcher(workerId).matches()) {
                register(
                        workerId,
                        cleanBaseUrl + "/workers/" + workerId
                );
            }
        }
    }

    public Collection<WorkerMetadata> getAll() {
        return workers.values();
    }
}
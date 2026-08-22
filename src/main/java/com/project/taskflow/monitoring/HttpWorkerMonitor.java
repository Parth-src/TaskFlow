package com.project.taskflow.monitoring;

import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class HttpWorkerMonitor
        implements WorkerMonitor {

    private final WorkerRegistry registry;

    private final HttpClient client =
            HttpClient.newHttpClient();

    public HttpWorkerMonitor(
            WorkerRegistry registry) {

        this.registry = registry;
    }

    @Override
    public boolean isHealthy(
            WorkerMetadata worker) {

        try {

            URI workerEndpoint =
                    URI.create(
                            worker.getEndpoint()
                    );

            URI healthEndpoint =
                    new URI(
                            workerEndpoint.getScheme(),
                            workerEndpoint.getAuthority(),
                            "/health",
                            null,
                            null
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(healthEndpoint)
                            .GET()
                            .timeout(
                                    java.time.Duration.ofSeconds(2)
                            )
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            return response.statusCode() == 200;

        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public List<WorkerStatus> getWorkers() {

        List<WorkerStatus> statuses =
                new ArrayList<>();

        for (WorkerMetadata worker :
                registry.getAll()) {

            boolean healthy =
                    isHealthy(worker);

            statuses.add(
                    new WorkerStatus(
                            worker.getWorkerId(),
                            worker.getEndpoint(),
                            healthy
                    )
            );
        }

        return statuses;
    }
}
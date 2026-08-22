package com.project.taskflow.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WorkerDiscovery {

    private final HttpClient client;

    private final ObjectMapper mapper;

    public WorkerDiscovery() {

        this.client =
                HttpClient.newHttpClient();

        this.mapper =
                new ObjectMapper();
    }

    public List<String> discover(
            String baseUrl) {

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl + "/workers"
                                    )
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                throw new RuntimeException(
                        "Worker discovery failed. HTTP status: "
                                + response.statusCode()
                );
            }

            JsonNode root =
                    mapper.readTree(
                            response.body()
                    );

            List<String> workers =
                    new ArrayList<>();

            for (JsonNode worker :
                    root.get("workers")) {

                workers.add(
                        worker.asText()
                );
            }

            return workers;

        } catch (IOException |
                 InterruptedException e) {

            throw new RuntimeException(
                    "Failed to discover workers",
                    e
            );
        }
    }
}
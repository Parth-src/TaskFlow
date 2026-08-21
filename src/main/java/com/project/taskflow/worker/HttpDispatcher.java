package com.project.taskflow.worker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpDispatcher {

    private final HttpClient client =
            HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public WorkerResponse dispatch(
            WorkerMetadata worker) {

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()

                            .uri(
                                    URI.create(
                                            worker.getEndpoint()
                                    )
                            )

                            .POST(
                                    HttpRequest
                                            .BodyPublishers
                                            .noBody()
                            )

                            .build();

            HttpResponse<String> response =
                    client.send(

                            request,

                            HttpResponse
                                    .BodyHandlers
                                    .ofString()
                    );

            System.out.println(
                    response.body()
            );

            if (response.statusCode() == 200) {

                WorkerResponse workerResponse =
                        objectMapper.readValue(
                                response.body(),
                                WorkerResponse.class
                        );

                return workerResponse;
            }

            return new WorkerResponse(
                    false,
                    true,
                    "Worker returned HTTP "
                            + response.statusCode()
            );

        } catch (IOException e) {

            return new WorkerResponse(
                    false,
                    true,
                    "Worker connection failed"
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return new WorkerResponse(
                    false,
                    true,
                    "Worker execution interrupted"
            );
        }
    }
}
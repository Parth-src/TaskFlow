package com.project.taskflow.worker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpDispatcher {

    private final HttpClient client =
            HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private final String workerToken;


    public HttpDispatcher(
            String workerToken) {

        if (workerToken == null ||
                workerToken.isBlank()) {

            throw new IllegalStateException(
                    "Worker token is not configured"
            );
        }

        this.workerToken = workerToken;
    }


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

                            .header(
                                    "Authorization",
                                    "Bearer "
                                            + workerToken
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

                return objectMapper.readValue(
                        response.body(),
                        WorkerResponse.class
                );
            }


            int status =
                    response.statusCode();


            if (status == 401 ||
                    status == 403) {

                return new WorkerResponse(
                        false,
                        false,
                        "Worker authentication failed: HTTP "
                                + status
                );
            }


            if (status >= 500) {

                return new WorkerResponse(
                        false,
                        true,
                        "Worker returned HTTP "
                                + status
                );
            }


            return new WorkerResponse(
                    false,
                    false,
                    "Worker returned HTTP "
                            + status
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
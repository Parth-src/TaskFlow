package com.project.taskflow.worker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpDispatcher {

    private final HttpClient client =
            HttpClient.newHttpClient();

    public boolean dispatch(
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

            return response.statusCode()
                    == 200;

        } catch (IOException |
                 InterruptedException e) {

            e.printStackTrace();

            return false;
        }
    }
}

package com.project.taskflow.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestConnection {

    public static void main(String[] args)
            throws Exception {

        HttpClient client =
                HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://127.0.0.1:8081/workers"
                                )
                        )
                        .GET()
                        .build();

        System.out.println(
                "Sending request..."
        );

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println(
                "Status: "
                        + response.statusCode()
        );

        System.out.println(
                "Body: "
                        + response.body()
        );
    }
}

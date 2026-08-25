package com.project.taskflow.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskflow.user.User;
import com.project.taskflow.user.UserRepository;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GitHubOAuthService {

    private final GitHubOAuthProperties properties;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final SessionService sessionService;

    private final HttpClient httpClient;

    public GitHubOAuthService(
            GitHubOAuthProperties properties,
            UserRepository userRepository,
            SessionService sessionService) {

        this.properties = properties;

        this.userRepository = userRepository;

        this.sessionService = sessionService;

        this.objectMapper =
                new ObjectMapper();

        this.httpClient =
                HttpClient.newHttpClient();
    }

    public String login(
            String authorizationCode) {

        try {

            String accessToken =
                    exchangeCodeForToken(
                            authorizationCode
                    );

            JsonNode githubUser =
                    getGithubUser(
                            accessToken
                    );

            String githubId =
                    githubUser
                            .get("id")
                            .asText();

            String username =
                    githubUser
                            .get("login")
                            .asText();

            String email =
                    githubUser.has("email")
                            && !githubUser
                            .get("email")
                            .isNull()
                            ? githubUser
                            .get("email")
                            .asText()
                            : null;

            User user =
                    userRepository
                            .findByGithubId(
                                    githubId
                            )
                            .orElseGet(
                                    () -> {

                                        User newUser =
                                                new User(
                                                        githubId,
                                                        username,
                                                        email
                                                );

                                        return userRepository
                                                .save(
                                                        newUser
                                                );
                                    }
                            );

            return sessionService.createSession(user);

        } catch (Exception e) {

            throw new RuntimeException(
                    "GitHub authentication failed",
                    e
            );
        }
    }

    private String exchangeCodeForToken(
            String code)
            throws Exception {

        String body =
                "client_id="
                        + encode(
                        properties.getClientId()
                )
                        + "&client_secret="
                        + encode(
                        properties.getClientSecret()
                )
                        + "&code="
                        + encode(code);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://github.com/login/oauth/access_token"
                                )
                        )
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(body)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println(
                "GitHub token response status: "
                        + response.statusCode()
        );

        System.out.println(
                "GitHub token response body: "
                        + response.body()
        );

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "GitHub token exchange failed: "
                            + response.body()
            );
        }

        JsonNode json =
                objectMapper.readTree(
                        response.body()
                );

        JsonNode token =
                json.get("access_token");

        if (token == null) {

            throw new RuntimeException(
                    "GitHub did not return an access token. Response: "
                            + response.body()
            );
        }

        return token.asText();
    }

    private String encode(String value) {

        return java.net.URLEncoder
                .encode(
                        value,
                        java.nio.charset.StandardCharsets.UTF_8
                );
    }

    private JsonNode getGithubUser(
            String accessToken)
            throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.github.com/user"
                                )
                        )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .header(
                                "Accept",
                                "application/vnd.github+json"
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println(
                "GitHub user response status: "
                        + response.statusCode()
        );

        System.out.println(
                "GitHub user response body: "
                        + response.body()
        );

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "GitHub user request failed: "
                            + response.body()
            );
        }

        return objectMapper.readTree(
                response.body()
        );
    }
}
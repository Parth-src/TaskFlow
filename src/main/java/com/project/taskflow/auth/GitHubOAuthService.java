package com.project.taskflow.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskflow.dashboard.dto.GitHubRepositoryDTO;
import com.project.taskflow.user.User;
import com.project.taskflow.user.UserRepository;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

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
                            .map(
                                    existingUser -> {
                                        existingUser.setGithubAccessToken(accessToken);
                                        existingUser.setUsername(username);
                                        if (email != null) {
                                            existingUser.setEmail(email);
                                        }
                                        return userRepository.save(existingUser);
                                    }
                            )
                            .orElseGet(
                                    () -> {

                                        User newUser =
                                                new User(
                                                        githubId,
                                                        username,
                                                        email
                                                );
                                        newUser.setGithubAccessToken(accessToken);

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

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "GitHub token exchange failed with status: "
                            + response.statusCode()
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
                    "GitHub did not return an access token."
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

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "GitHub user request failed with status: "
                            + response.statusCode()
            );
        }

        return objectMapper.readTree(
                response.body()
        );
    }

    public List<GitHubRepositoryDTO> fetchUserRepositories(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return List.of();
        }

        List<GitHubRepositoryDTO> result = new ArrayList<>();
        int page = 1;
        int maxPages = 10; // Supports up to 1,000 repositories via pagination

        try {
            while (page <= maxPages) {
                String uri = "https://api.github.com/user/repos?per_page=100&page="
                        + page
                        + "&sort=updated&affiliation=owner,collaborator,organization_member";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() != 200) {
                    System.err.println("GitHub repositories fetch returned status " + response.statusCode() + ": " + response.body());
                    break;
                }

                JsonNode root = objectMapper.readTree(response.body());
                if (!root.isArray() || root.isEmpty()) {
                    break;
                }

                for (JsonNode repo : root) {
                    String name = repo.path("name").asText("");
                    String fullName = repo.path("full_name").asText(name);
                    String owner = repo.path("owner").path("login").asText("");
                    String description = repo.hasNonNull("description") ? repo.path("description").asText("") : "";
                    String defaultBranch = repo.hasNonNull("default_branch") ? repo.path("default_branch").asText("main") : "main";
                    boolean isPrivate = repo.path("private").asBoolean(false);

                    result.add(new GitHubRepositoryDTO(
                            name,
                            fullName,
                            owner,
                            description,
                            defaultBranch,
                            isPrivate
                    ));
                }

                if (root.size() < 100) {
                    break;
                }

                page++;
            }
        } catch (Exception e) {
            System.err.println("Error fetching GitHub repositories: " + e.getMessage());
        }

        return result;
    }
}
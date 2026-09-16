package com.project.taskflow.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth/github")
public class GitHubAuthController {

    private final GitHubOAuthProperties properties;
    private final GitHubOAuthService githubOAuthService;
    private final SessionService sessionService;

    @Value("${taskflow.frontend-url:http://localhost:5173/#/dashboard}")
    private String frontendUrl;

    public GitHubAuthController(
            GitHubOAuthProperties properties,
            GitHubOAuthService githubOAuthService,
            SessionService sessionService) {

        this.properties =
                properties;

        this.githubOAuthService =
                githubOAuthService;

        this.sessionService =
                sessionService;
    }

    @GetMapping
    public void login(
            HttpServletResponse response)
            throws Exception {

        String url =
                "https://github.com/login/oauth/authorize"
                        + "?client_id="
                        + encode(
                        properties.getClientId()
                )
                        + "&redirect_uri="
                        + encode(
                        properties.getRedirectUri()
                )
                        + "&scope="
                        + encode(
                        "read:user user:email repo"
                );

        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public void callback(
            @RequestParam String code,
            HttpServletResponse response)
            throws Exception {

        String sessionToken =
                githubOAuthService.login(
                        code
                );

        Cookie cookie =
                new Cookie(
                        "TASKFLOW_SESSION",
                        sessionToken
                );

        cookie.setHttpOnly(true);
        cookie.setSecure(false); // localhost
        cookie.setPath("/");
        cookie.setMaxAge(
            7 * 24 * 60 * 60
        );

        response.addCookie(cookie);

        response.sendRedirect(
                frontendUrl
        );
    }

    private String encode(
            String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
package com.project.taskflow.auth;

import com.project.taskflow.user.User;
import com.project.taskflow.user.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthController(
            UserRepository userRepository,
            SessionService sessionService) {

        this.userRepository =
                userRepository;

        this.sessionService =
                sessionService;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {

        UUID userId = UserContext.get();
        if (userId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Not authenticated"
            );
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.UNAUTHORIZED,
                                "User not found"
                        ));

        Map<String, Object> userData =
                new HashMap<>();

        userData.put(
                "id",
                user.getId()
        );

        userData.put(
                "githubId",
                user.getGithubId()
        );

        userData.put(
                "username",
                user.getUsername()
        );

        userData.put(
                "email",
                user.getEmail()
        );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "authenticated",
                true
        );

        response.put(
                "user",
                userData
        );

        return response;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String token =
                extractSessionToken(
                        request
                );

        sessionService.revoke(
                token
        );

        Cookie cookie =
                new Cookie(
                        "TASKFLOW_SESSION",
                        ""
                );

        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return Map.of(
                "success",
                true
        );
    }

    @PostMapping("/dev-login")
    public Map<String, Object> devLogin(
            @RequestBody(required = false) Map<String, String> body,
            HttpServletResponse response) {

        String username = (body != null && body.containsKey("username") && !body.get("username").isBlank())
                ? body.get("username") : "dev-user";
        String email = (body != null && body.containsKey("email") && !body.get("email").isBlank())
                ? body.get("email") : username + "@taskflow.dev";
        String githubId = "gh-" + username.toLowerCase().replaceAll("[^a-z0-9]", "-");

        User user = userRepository.findByGithubId(githubId).orElseGet(() -> {
            User newUser = new User(githubId, username, email);
            return userRepository.save(newUser);
        });

        String sessionToken = sessionService.createSession(user);

        Cookie cookie = new Cookie("TASKFLOW_SESSION", sessionToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("githubId", user.getGithubId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());

        Map<String, Object> res = new HashMap<>();
        res.put("authenticated", true);
        res.put("user", userData);

        return res;
    }

    private String extractSessionToken(
            HttpServletRequest request) {

        Cookie[] cookies =
                request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if ("TASKFLOW_SESSION"
                    .equals(cookie.getName())) {

                return cookie.getValue();
            }
        }

        return null;
    }
}
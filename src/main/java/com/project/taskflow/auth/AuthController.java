package com.project.taskflow.auth;

import com.project.taskflow.user.User;
import com.project.taskflow.user.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

        User user =
                userRepository
                        .findById(
                                UserContext.require()
                        )
                        .orElseThrow();

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
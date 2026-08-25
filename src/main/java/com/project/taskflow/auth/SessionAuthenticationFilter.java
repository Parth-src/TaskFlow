package com.project.taskflow.auth;

import com.project.taskflow.user.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionAuthenticationFilter
        extends OncePerRequestFilter {

    private final SessionService sessionService;

    public SessionAuthenticationFilter(
            SessionService sessionService) {

        this.sessionService =
                sessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token =
                extractSessionToken(request);

        User user =
                sessionService.authenticate(
                        token
                );

        if (user != null) {

            UserContext.set(
                    user.getId()
            );
        }

        try {

            filterChain.doFilter(
                    request,
                    response
            );

        } finally {

            UserContext.clear();
        }
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
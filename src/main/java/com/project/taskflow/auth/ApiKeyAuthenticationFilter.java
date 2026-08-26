package com.project.taskflow.auth;

import com.project.taskflow.credential.ApiKeyAuthenticationService;
import com.project.taskflow.credential.ApiKeyHasher;
import com.project.taskflow.credential.ProjectCredential;
import com.project.taskflow.credential.ProjectCredentialRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthenticationFilter
        extends OncePerRequestFilter {

    private final ProjectCredentialRepository credentialRepository;
    private final ApiKeyHasher keyHasher;
    private final ApiKeyAuthenticationService authenticationService;

    public ApiKeyAuthenticationFilter(
            ProjectCredentialRepository credentialRepository,
            ApiKeyHasher keyHasher,
            ApiKeyAuthenticationService authenticationService) {

        this.credentialRepository =
                credentialRepository;

        this.keyHasher =
                keyHasher;

        this.authenticationService =
                authenticationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String apiKey =
                    request.getHeader("X-API-Key");

            if (apiKey == null ||
                    apiKey.isBlank()) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            ProjectCredential credential =
                    authenticationService.authenticate(
                            apiKey
                    );

            ProjectContext.set(
                    credential
                            .getProject()
                            .getId()
            );

            try {

                filterChain.doFilter(
                        request,
                        response
                );

            } finally {

                ProjectContext.clear();
            }

        } catch (Exception e) {

            unauthorized(response);
        }
    }

    private void unauthorized(
            HttpServletResponse response)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write(
                """
                {
                    "success": false,
                    "message": "Invalid or revoked TaskFlow API key"
                }
                """
        );
    }
}
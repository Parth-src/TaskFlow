package com.project.taskflow.auth;

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

    public ApiKeyAuthenticationFilter(
            ProjectCredentialRepository credentialRepository,
            ApiKeyHasher keyHasher) {

        this.credentialRepository =
                credentialRepository;

        this.keyHasher =
                keyHasher;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String authHeader =
                    request.getHeader(
                            "Authorization"
                    );

            /*
             * No API key.
             *
             * We don't reject every request here because
             * dashboard/authentication endpoints will eventually
             * use a different authentication mechanism.
             */
            if (authHeader == null ||
                    !authHeader.startsWith("Bearer ")) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            String apiKey =
                    authHeader.substring(
                            "Bearer ".length()
                    ).trim();

            if (apiKey.isEmpty()) {

                unauthorized(response);

                return;
            }

            String hash =
                    keyHasher.hash(apiKey);

            ProjectCredential credential =
                    credentialRepository
                            .findByKeyHash(hash)
                            .orElse(null);

            if (credential == null ||
                    credential.isRevoked()) {

                unauthorized(response);

                return;
            }

            credential.markUsed();

            credentialRepository.save(
                    credential
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
package com.project.taskflow.auth;

import com.project.taskflow.credential.ApiKeyHasher;
import com.project.taskflow.user.User;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final SessionTokenService tokenService;
    private final ApiKeyHasher hasher;

    public SessionService(
            UserSessionRepository sessionRepository,
            SessionTokenService tokenService,
            ApiKeyHasher hasher) {

        this.sessionRepository =
                sessionRepository;

        this.tokenService =
                tokenService;

        this.hasher =
                hasher;
    }

    public String createSession(
            User user) {

        String token =
                tokenService.generate();

        String tokenHash =
                hasher.hash(token);

        Instant expiresAt =
                Instant.now()
                        .plus(
                                7,
                                ChronoUnit.DAYS
                        );

        UserSession session =
                new UserSession(
                        user,
                        tokenHash,
                        expiresAt
                );

        sessionRepository.save(
                session
        );

        return token;
    }

    public User authenticate(
            String token) {

        if (token == null ||
                token.isBlank()) {

            return null;
        }

        String tokenHash =
                hasher.hash(token);

        UserSession session =
                sessionRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElse(null);

        if (session == null ||
                !session.isValid()) {

            return null;
        }

        return session.getUser();
    }

    public void revoke(
            String token) {

        if (token == null ||
                token.isBlank()) {

            return;
        }

        String tokenHash =
                hasher.hash(token);

        sessionRepository
                .findByTokenHash(
                        tokenHash
                )
                .ifPresent(
                        session -> {

                            session.revoke();

                            sessionRepository.save(
                                    session
                            );
                        }
                );
    }
}
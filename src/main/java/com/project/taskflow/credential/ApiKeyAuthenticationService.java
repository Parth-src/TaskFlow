package com.project.taskflow.credential;

import org.springframework.stereotype.Service;

@Service
public class ApiKeyAuthenticationService {

    private final ProjectCredentialRepository credentialRepository;
    private final ApiKeyHasher keyHasher;

    public ApiKeyAuthenticationService(
            ProjectCredentialRepository credentialRepository,
            ApiKeyHasher keyHasher) {

        this.credentialRepository =
                credentialRepository;

        this.keyHasher =
                keyHasher;
    }

    public ProjectCredential authenticate(
            String apiKey) {

        if (apiKey == null ||
                apiKey.isBlank()) {

            throw new IllegalArgumentException(
                    "API key is required"
            );
        }

        String hash =
                keyHasher.hash(
                        apiKey.trim()
                );

        ProjectCredential credential =
                credentialRepository
                        .findByKeyHash(hash)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Invalid TaskFlow API key"
                                )
                        );

        if (credential.isRevoked()) {

            throw new IllegalArgumentException(
                    "TaskFlow API key has been revoked"
            );
        }

        credential.markUsed();

        credentialRepository.save(
                credential
        );

        return credential;
    }
}
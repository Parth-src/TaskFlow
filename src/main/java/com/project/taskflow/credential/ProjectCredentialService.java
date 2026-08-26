package com.project.taskflow.credential;

import com.project.taskflow.credential.dto.ApiKeyDTO;
import com.project.taskflow.credential.dto.CreatedApiKeyResponse;
import com.project.taskflow.project.Project;
import com.project.taskflow.project.ProjectAuthorizationService;
import com.project.taskflow.project.ProjectRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectCredentialService {

    private final ProjectCredentialRepository credentialRepository;
    private final ApiKeyGenerator keyGenerator;
    private final ApiKeyHasher keyHasher;
    private final ProjectAuthorizationService authorizationService;

    public ProjectCredentialService(
            ProjectCredentialRepository credentialRepository,
            ProjectAuthorizationService authorizationService,
            ApiKeyGenerator keyGenerator,
            ApiKeyHasher keyHasher) {

        this.credentialRepository =
                credentialRepository;

        this.authorizationService =
                authorizationService;

        this.keyGenerator =
                keyGenerator;

        this.keyHasher =
                keyHasher;
    }

    public CreatedApiKeyResponse create(
            UUID projectId,
            UUID userId,
            String name,
            CredentialEnvironment environment) {

        Project project =
                authorizationService.getOwnedProject(
                        projectId,
                        userId
                );

        if (name == null ||
                name.isBlank()) {

            throw new IllegalArgumentException(
                    "Credential name is required"
            );
        }

        if (environment == null) {

            throw new IllegalArgumentException(
                    "Credential environment is required"
            );
        }

        String apiKey =
                keyGenerator.generate(
                        environment
                );

        String keyHash =
                keyHasher.hash(
                        apiKey
                );

        ProjectCredential credential =
                new ProjectCredential(
                        project,
                        name.trim(),
                        keyHash,
                        environment
                );

        credential =
                credentialRepository.save(
                        credential
                );

        return new CreatedApiKeyResponse(
                credential.getId(),
                credential.getName(),
                credential.getEnvironment(),
                apiKey
        );
    }

    public List<ApiKeyDTO> getAll(
            UUID projectId,
            UUID userId) {

        authorizationService.getOwnedProject(
                projectId,
                userId
        );

        return credentialRepository
                .findByProjectId(projectId)
                .stream()
                .map(ApiKeyDTO::new)
                .toList();
    }

    public void revoke(
            UUID projectId,
            UUID credentialId,
            UUID userId) {

        authorizationService.getOwnedProject(
                projectId,
                userId
        );

        ProjectCredential credential =
                credentialRepository
                        .findById(credentialId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Credential not found"
                                )
                        );

        if (!credential
                .getProject()
                .getId()
                .equals(projectId)) {

            throw new RuntimeException(
                    "Credential does not belong to project"
            );
        }

        credential.revoke();

        credentialRepository.save(
                credential
        );
    }
}
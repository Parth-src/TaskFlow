package com.project.taskflow.credential;

import com.project.taskflow.credential.dto.ApiKeyDTO;
import com.project.taskflow.credential.dto.CreatedApiKeyResponse;
import com.project.taskflow.project.Project;
import com.project.taskflow.project.ProjectRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectCredentialService {

    private final ProjectCredentialRepository credentialRepository;
    private final ProjectRepository projectRepository;
    private final ApiKeyGenerator keyGenerator;
    private final ApiKeyHasher keyHasher;

    public ProjectCredentialService(
            ProjectCredentialRepository credentialRepository,
            ProjectRepository projectRepository,
            ApiKeyGenerator keyGenerator,
            ApiKeyHasher keyHasher) {

        this.credentialRepository =
                credentialRepository;

        this.projectRepository =
                projectRepository;

        this.keyGenerator =
                keyGenerator;

        this.keyHasher =
                keyHasher;
    }

    public CreatedApiKeyResponse create(
            UUID projectId,
            String name,
            CredentialEnvironment environment) {

        Project project =
                projectRepository
                        .findById(projectId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Project not found"
                                )
                        );

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
                        name,
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
            UUID projectId) {

        return credentialRepository
                .findByProjectId(projectId)
                .stream()
                .map(ApiKeyDTO::new)
                .toList();
    }

    public void revoke(
            UUID projectId,
            UUID credentialId) {

        ProjectCredential credential =
                credentialRepository
                        .findById(
                                credentialId
                        )
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
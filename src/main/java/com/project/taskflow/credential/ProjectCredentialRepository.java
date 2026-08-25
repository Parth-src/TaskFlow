package com.project.taskflow.credential;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectCredentialRepository
        extends JpaRepository<ProjectCredential, UUID> {

    List<ProjectCredential> findByProjectId(
            UUID projectId
    );

    Optional<ProjectCredential> findByKeyHash(
            String keyHash
    );
}
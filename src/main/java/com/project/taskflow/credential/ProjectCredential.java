package com.project.taskflow.credential;

import com.project.taskflow.project.Project;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_credentials",
        indexes = {
                @Index(
                        name = "idx_credentials_project_id",
                        columnList = "project_id"
                )
        }
)
public class ProjectCredential {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private Project project;

    @Column(
            nullable = false
    )
    private String name;

    @Column(
            name = "key_hash",
            nullable = false,
            unique = true
    )
    private String keyHash;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false
    )
    private CredentialEnvironment environment;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column
    private Instant lastUsedAt;

    @Column
    private Instant revokedAt;

    protected ProjectCredential() {
    }

    public ProjectCredential(
            Project project,
            String name,
            String keyHash,
            CredentialEnvironment environment) {

        this.project = project;
        this.name = name;
        this.keyHash = keyHash;
        this.environment = environment;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public CredentialEnvironment getEnvironment() {
        return environment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
package com.project.taskflow.user;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_github_id",
                        columnNames = "github_id"
                )
        }
)
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(
            name = "github_id",
            nullable = false,
            unique = true
    )
    private String githubId;

    @Column(
            nullable = false
    )
    private String username;

    @Column
    private String email;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected User() {
    }

    public User(
            String githubId,
            String username,
            String email) {

        this.githubId = githubId;
        this.username = username;
        this.email = email;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getGithubId() {
        return githubId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
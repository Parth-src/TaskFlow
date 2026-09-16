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
            name = "github_access_token"
    )
    private String githubAccessToken;

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

    public String getGithubAccessToken() {
        return githubAccessToken;
    }

    public void setGithubAccessToken(String githubAccessToken) {
        this.githubAccessToken = githubAccessToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
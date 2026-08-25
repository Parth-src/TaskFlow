package com.project.taskflow.auth;

import com.project.taskflow.user.User;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_sessions",
        indexes = {
                @Index(
                        name = "idx_user_sessions_token_hash",
                        columnList = "token_hash",
                        unique = true
                ),
                @Index(
                        name = "idx_user_sessions_user_id",
                        columnList = "user_id"
                )
        }
)
public class UserSession {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true
    )
    private String tokenHash;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            nullable = false
    )
    private Instant expiresAt;

    @Column
    private Instant revokedAt;

    protected UserSession() {
    }

    public UserSession(
            User user,
            String tokenHash,
            Instant expiresAt) {

        this.user = user;
        this.tokenHash = tokenHash;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public boolean isValid() {

        return revokedAt == null
                && Instant.now().isBefore(expiresAt);
    }
}
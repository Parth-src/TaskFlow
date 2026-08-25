package com.project.taskflow.project;

import com.project.taskflow.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "projects",
        indexes = {
                @Index(
                        name = "idx_projects_user_id",
                        columnList = "user_id"
                )
        }
)
public class Project {

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
            nullable = false
    )
    private String name;

    @Column(
            name = "github_repository"
    )
    private String githubRepository;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected Project() {
    }

    public Project(
            User user,
            String name) {

        this.user = user;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getGithubRepository() {
        return githubRepository;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void connectGithubRepository(
            String repository) {

        this.githubRepository = repository;
    }
}
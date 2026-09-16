package com.project.taskflow.dashboard.dto;

public class GitHubRepositoryDTO {

    private final String name;
    private final String fullName;
    private final String owner;
    private final String description;
    private final String defaultBranch;
    private final boolean isPrivate;

    public GitHubRepositoryDTO(
            String name,
            String fullName,
            String owner,
            String description,
            String defaultBranch,
            boolean isPrivate) {

        this.name = name;
        this.fullName = fullName;
        this.owner = owner;
        this.description = description;
        this.defaultBranch = defaultBranch;
        this.isPrivate = isPrivate;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}

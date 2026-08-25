package com.project.taskflow.credential.dto;

import com.project.taskflow.credential.CredentialEnvironment;

import java.util.UUID;

public class CreatedApiKeyResponse {

    private final UUID credentialId;
    private final String name;
    private final CredentialEnvironment environment;

    /*
     * This is returned only during creation.
     */
    private final String apiKey;

    public CreatedApiKeyResponse(
            UUID credentialId,
            String name,
            CredentialEnvironment environment,
            String apiKey) {

        this.credentialId = credentialId;
        this.name = name;
        this.environment = environment;
        this.apiKey = apiKey;
    }

    public UUID getCredentialId() {
        return credentialId;
    }

    public String getName() {
        return name;
    }

    public CredentialEnvironment getEnvironment() {
        return environment;
    }

    public String getApiKey() {
        return apiKey;
    }
}
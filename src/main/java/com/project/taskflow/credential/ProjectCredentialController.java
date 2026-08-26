package com.project.taskflow.credential;

import com.project.taskflow.auth.UserContext;
import com.project.taskflow.credential.dto.ApiKeyDTO;
import com.project.taskflow.credential.dto.CreatedApiKeyResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/projects/{projectId}/credentials"
)
public class ProjectCredentialController {

    private final ProjectCredentialService credentialService;

    public ProjectCredentialController(
            ProjectCredentialService credentialService) {

        this.credentialService =
                credentialService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedApiKeyResponse create(
            @PathVariable UUID projectId,
            @RequestBody CreateCredentialRequest request) {

        UUID userId =
                UserContext.require();

        return credentialService.create(
                projectId,
                userId,
                request.name(),
                request.environment()
        );
    }

    @GetMapping
    public List<ApiKeyDTO> getAll(
            @PathVariable UUID projectId) {

        UUID userId =
                UserContext.require();

        return credentialService.getAll(
                projectId,
                userId
        );
    }

    @DeleteMapping("/{credentialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable UUID projectId,
            @PathVariable UUID credentialId) {

        UUID userId =
                UserContext.require();

        credentialService.revoke(
                projectId,
                credentialId,
                userId
        );
    }

    public record CreateCredentialRequest(
            String name,
            CredentialEnvironment environment
    ) {
    }
}
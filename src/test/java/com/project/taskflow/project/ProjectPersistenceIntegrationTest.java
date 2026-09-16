package com.project.taskflow.project;

import com.project.taskflow.credential.ApiKeyHasher;
import com.project.taskflow.credential.CredentialEnvironment;
import com.project.taskflow.credential.ProjectCredential;
import com.project.taskflow.credential.ProjectCredentialRepository;
import com.project.taskflow.credential.ProjectCredentialService;
import com.project.taskflow.credential.dto.CreatedApiKeyResponse;
import com.project.taskflow.user.User;
import com.project.taskflow.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProjectPersistenceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectAuthorizationService authorizationService;

    @Autowired
    private ProjectCredentialService credentialService;

    @Autowired
    private ProjectCredentialRepository credentialRepository;

    @Autowired
    private ApiKeyHasher keyHasher;

    @Test
    void testUserAndProjectPersistence() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("gh-" + uniqueSuffix, "user-" + uniqueSuffix, "user-" + uniqueSuffix + "@example.com");
        user = userRepository.save(user);
        assertNotNull(user.getId());

        // Create project
        Project project = projectService.createProject(user.getId(), "Project-" + uniqueSuffix);
        assertNotNull(project.getId());
        assertEquals(user.getId(), project.getUser().getId());

        // Verify project in database
        Project fetched = projectRepository.findById(project.getId()).orElse(null);
        assertNotNull(fetched);
        assertEquals("Project-" + uniqueSuffix, fetched.getName());

        // Create API key credential
        CreatedApiKeyResponse keyResponse = credentialService.create(
                project.getId(),
                user.getId(),
                "test-key",
                CredentialEnvironment.PRODUCTION
        );
        assertNotNull(keyResponse.getApiKey());
        assertTrue(keyResponse.getApiKey().startsWith("tf_live_"));

        // Verify credential in database stores SHA-256 hash, NOT plaintext
        ProjectCredential storedCred = credentialRepository.findById(keyResponse.getCredentialId()).orElseThrow();
        assertNotEquals(keyResponse.getApiKey(), storedCred.getKeyHash());
        assertEquals(keyHasher.hash(keyResponse.getApiKey()), storedCred.getKeyHash());
        assertEquals("PRODUCTION", storedCred.getEnvironment().name());

        // Revoke credential
        credentialService.revoke(project.getId(), keyResponse.getCredentialId(), user.getId());
        ProjectCredential revokedCred = credentialRepository.findById(keyResponse.getCredentialId()).orElseThrow();
        assertTrue(revokedCred.isRevoked());
        assertNotNull(revokedCred.getRevokedAt());

        // Test project authorization & isolation
        User otherUser = userRepository.save(new User("gh-other-" + uniqueSuffix, "other", "other@example.com"));
        assertThrows(ResponseStatusException.class, () ->
                authorizationService.getOwnedProject(project.getId(), otherUser.getId())
        );
    }
}

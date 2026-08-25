package com.project.taskflow.credential;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ApiKeyGenerator {

    private final SecureRandom random =
            new SecureRandom();

    public String generate(
            CredentialEnvironment environment) {

        byte[] bytes =
                new byte[32];

        random.nextBytes(bytes);

        String secret =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

        String prefix =
                environment ==
                        CredentialEnvironment.PRODUCTION
                        ? "tf_live_"
                        : "tf_test_";

        return prefix + secret;
    }
}
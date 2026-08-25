package com.project.taskflow.auth;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SessionTokenService {

    private final SecureRandom random =
            new SecureRandom();

    public String generate() {

        byte[] bytes =
                new byte[32];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
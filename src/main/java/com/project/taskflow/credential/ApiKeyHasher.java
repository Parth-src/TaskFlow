package com.project.taskflow.credential;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class ApiKeyHasher {

    public String hash(String key) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            key.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 unavailable",
                    e
            );
        }
    }
}
package com.project.taskflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskFlowConfigLoader {

    private final ObjectMapper mapper;

    private static final Pattern ENV_PATTERN =
            Pattern.compile(
                    "\\$\\{([^}]+)}"
            );

    public TaskFlowConfigLoader() {

        mapper =
                new ObjectMapper(
                        new YAMLFactory()
                );
    }

    public TaskFlowConfig load(
            Path path) {

        try {

            String yaml =
                    Files.readString(path);

            yaml =
                    resolveEnvironmentVariables(
                            yaml
                    );

            return mapper.readValue(
                    yaml,
                    TaskFlowConfig.class
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to load TaskFlow configuration: "
                            + path,
                    e
            );
        }
    }


    private String resolveEnvironmentVariables(
            String yaml) {

        Matcher matcher =
                ENV_PATTERN.matcher(yaml);

        StringBuffer result =
                new StringBuffer();

        while (matcher.find()) {

            String variableName =
                    matcher.group(1);

            String value =
                    System.getenv(
                            variableName
                    );

            if (value == null ||
                    value.isBlank()) {

                throw new IllegalStateException(
                        "Environment variable is not set: "
                                + variableName
                );
            }

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(
                            value
                    )
            );
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
package com.project.taskflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;

public class TaskFlowConfigLoader {

    private final ObjectMapper mapper;

    public TaskFlowConfigLoader() {

        mapper =
                new ObjectMapper(
                        new YAMLFactory()
                );
    }

    public TaskFlowConfig load(
            Path path) {

        try {

            return mapper.readValue(
                    path.toFile(),
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
}
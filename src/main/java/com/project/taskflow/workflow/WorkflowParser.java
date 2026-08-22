package com.project.taskflow.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;

public class WorkflowParser {

    private final ObjectMapper mapper;

    public WorkflowParser() {

        mapper = new ObjectMapper(
                new YAMLFactory()
        );
    }

    public WorkflowDefinition parse(
            Path path) {

        try {

            return mapper.readValue(
                    path.toFile(),
                    WorkflowDefinition.class
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to parse workflow: "
                            + path,
                    e
            );
        }
    }
}
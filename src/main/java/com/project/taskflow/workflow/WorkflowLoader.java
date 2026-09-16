package com.project.taskflow.workflow;

import com.project.taskflow.model.Workflow;

import java.nio.file.Path;

public class WorkflowLoader {

    private final WorkflowParser parser;

    private final WorkflowDefinitionBuilder builder;

    public WorkflowLoader() {

        this.parser =
                new WorkflowParser();

        this.builder =
                new WorkflowDefinitionBuilder();
    }

    public Workflow load(Path path) {

        WorkflowDefinition definition =
                parser.parse(path);

        return builder.build(definition);
    }

    public Workflow load(java.io.InputStream inputStream) {

        WorkflowDefinition definition =
                parser.parse(inputStream);

        return builder.build(definition);
    }

    public Workflow load(WorkflowDefinition definition) {

        return builder.build(definition);
    }
}
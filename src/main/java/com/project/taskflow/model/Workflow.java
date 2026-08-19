package com.project.taskflow.model;


import java.util.ArrayList;
import java.util.List;

public class Workflow {

    private final String name;

    private final List<WorkflowNode> nodes;

    public Workflow(String name) {

        this.name = name;

        this.nodes = new ArrayList<>();
    }

    public void addNode(WorkflowNode node) {

        nodes.add(node);
    }

    public List<WorkflowNode> getNodes() {

        return nodes;
    }

    public String getName() {

        return name;
    }
}
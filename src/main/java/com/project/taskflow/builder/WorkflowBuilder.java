package com.project.taskflow.builder;



import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import java.util.ArrayList;
import java.util.List;

public class WorkflowBuilder {

    private final Workflow workflow;

    private List<WorkflowNode> currentLayer;

    public WorkflowBuilder(String workflowName) {

        this.workflow = new Workflow(workflowName);

        this.currentLayer = new ArrayList<>();
    }

    public WorkflowBuilder task(String workerId) {

        WorkflowNode node =
                new WorkflowNode(workerId);

        workflow.addNode(node);

        currentLayer.clear();

        currentLayer.add(node);

        return this;
    }

    public WorkflowBuilder then(String workerId) {

        WorkflowNode node =
                new WorkflowNode(workerId);

        workflow.addNode(node);

        for (WorkflowNode parent : currentLayer) {

            parent.addDependent(node);

            node.addDependency(parent);
        }

        currentLayer.clear();

        currentLayer.add(node);

        return this;
    }

    public WorkflowBuilder parallel(String... workerIds) {

        List<WorkflowNode> nextLayer =
                new ArrayList<>();

        for (String workerId : workerIds) {

            WorkflowNode node =
                    new WorkflowNode(workerId);

            workflow.addNode(node);

            for (WorkflowNode parent : currentLayer) {

                parent.addDependent(node);

                node.addDependency(parent);
            }

            nextLayer.add(node);
        }

        currentLayer = nextLayer;

        return this;
    }

    public Workflow build() {

        return workflow;
    }
}

import React, { useState } from 'react';
import { Card } from '../components/common/Card';
import { CodeBlock } from '../components/common/CodeBlock';
import {
  BookOpen,
  Code2,
  FileCode,
  Shield,
  Zap,
  Server,
  Layers,
  CheckCircle2,
} from 'lucide-react';

export const DocumentationPage: React.FC = () => {
  const [activeSection, setActiveSection] = useState('quickstart');

  const yamlSpec = `name: payment-flow

tasks:
  - id: payment
    worker: payment

  - id: send-email
    worker: send-email
    depends_on:
      - payment

  - id: generate-invoice
    worker: generate-invoice
    depends_on:
      - payment

  - id: analytics
    worker: analytics
    depends_on:
      - payment`;

  const nodeSdk = `import { TaskFlow } from '@taskflow/sdk';

// Initialize TaskFlow client with your project API key
const taskflow = new TaskFlow({
  apiKey: process.env.TASKFLOW_API_KEY,
  endpoint: 'http://localhost:8080'
});

// Register worker business logic
taskflow.worker('payment', async (task) => {
  console.log('Processing payment for task:', task.id);
  const result = await stripe.charges.create(task.payload);
  return {
    success: true,
    message: 'Payment captured',
    transactionId: result.id
  };
});`;

  const pythonSdk = `from taskflow import TaskFlow

taskflow = TaskFlow(
    api_key="tf_live_your_key_here",
    endpoint="http://localhost:8080"
)

@taskflow.worker("generate-invoice")
def generate_invoice(task):
    invoice_pdf = build_invoice(task.payload)
    return {
        "success": True,
        "invoice_url": invoice_pdf.url
    }`;

  const httpWorkerSpec = `// TaskFlow Worker HTTP Dispatch Specification
// 1. TaskFlow dispatches an HTTP POST to worker endpoint
POST http://127.0.0.1:8081/workers/payment
Authorization: Bearer <TASKFLOW_WORKER_TOKEN>
Content-Type: application/json

// 2. Worker returns standardized JSON payload:
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Payment captured successfully",
  "shouldRetry": false
}

// 3. If worker fails with 5xx or transient exception:
HTTP/1.1 503 Service Unavailable
{
  "success": false,
  "message": "Database lock timeout",
  "shouldRetry": true
}`;

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <BookOpen size={22} color="var(--cyan-400)" />
            <span>Developer Documentation</span>
          </h1>
          <p className="page-subtitle">
            Architecture principles, workflow definitions, and polyglot worker HTTP protocols.
          </p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '240px 1fr', gap: '24px' }}>
        {/* Navigation Sidebar */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          {[
            { id: 'quickstart', label: 'Quick Start', icon: Zap },
            { id: 'yaml-spec', label: 'YAML Specification', icon: FileCode },
            { id: 'worker-proto', label: 'Worker HTTP Protocol', icon: Server },
            { id: 'sdks', label: 'SDK Integration', icon: Code2 },
            { id: 'reliability', label: 'Retries & DLQ', icon: Shield },
          ].map((item) => {
            const Icon = item.icon;
            const isActive = activeSection === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveSection(item.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '8px 12px',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '13px',
                  fontWeight: isActive ? 600 : 400,
                  background: isActive ? 'rgba(6, 182, 212, 0.1)' : 'transparent',
                  color: isActive ? 'var(--cyan-400)' : 'var(--text-secondary)',
                  textAlign: 'left',
                  borderLeft: isActive ? '2px solid var(--cyan-400)' : '2px solid transparent',
                }}
              >
                <Icon size={15} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>

        {/* Content Area */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {activeSection === 'quickstart' && (
            <Card title="Quick Start with TaskFlow">
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '16px' }}>
                TaskFlow enables you to remove long-running business logic from synchronous API request paths, returning instant <code>202 Accepted</code> responses to your clients while TaskFlow orchestrates background execution.
              </p>

              <h4 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '8px' }}>
                1. Submit a Workflow via API
              </h4>
              <CodeBlock
                code={`curl -X POST "http://localhost:8080/api/dashboard/workflows/payment-flow/execute?projectId=YOUR_PROJECT_ID" \\
  -H "X-API-Key: YOUR_API_KEY"`}
                language="bash"
                title="Execute Workflow"
              />

              <h4 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)', marginTop: '20px', marginBottom: '8px' }}>
                2. Instant API Response
              </h4>
              <CodeBlock
                code={`{
  "executionId": "e6a2b841-f739-4cb6-a4f7-8763528b1234",
  "workflowId": "payment-flow",
  "status": "QUEUED",
  "projectId": "859c23a1-...",
  "createdAt": "2026-09-09T18:00:00Z"
}`}
                language="json"
                title="Response"
              />
            </Card>
          )}

          {activeSection === 'yaml-spec' && (
            <Card title="Workflow YAML Specification">
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '16px' }}>
                Workflows are declarative Directed Acyclic Graphs (DAGs). Tasks without <code>depends_on</code> begin execution immediately. Dependent tasks unlock the instant all prerequisites complete.
              </p>
              <CodeBlock code={yamlSpec} language="yaml" title="src/main/resources/workflows/payment-flow.yaml" />
            </Card>
          )}

          {activeSection === 'worker-proto' && (
            <Card title="Language-Independent Worker HTTP Protocol">
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '16px' }}>
                TaskFlow dispatches tasks over standard HTTP with bearer tokens. Workers can run in Java, Python, Go, Node.js, C#, or Rust.
              </p>
              <CodeBlock code={httpWorkerSpec} language="http" title="Worker Contract" />
            </Card>
          )}

          {activeSection === 'sdks' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <Card title="TypeScript / Node.js Worker SDK">
                <CodeBlock code={nodeSdk} language="typescript" title="worker.ts" />
              </Card>

              <Card title="Python Worker SDK">
                <CodeBlock code={pythonSdk} language="python" title="worker.py" />
              </Card>
            </div>
          )}

          {activeSection === 'reliability' && (
            <Card title="Retry Policies & Dead Letter Queue">
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '16px' }}>
                TaskFlow guarantees execution reliability. If worker dispatch fails due to network partitions or worker 5xx errors:
              </p>

              <ul style={{ paddingLeft: '20px', fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.8, marginBottom: '16px' }}>
                <li><strong>Retry Policy:</strong> Failed tasks are retried up to 3 times before exhaustion.</li>
                <li><strong>Dead Letter Queue:</strong> Exhausted tasks are preserved in Redis DLQ along with error reason and attempt count.</li>
                <li><strong>Manual Reprocessing:</strong> DLQ tasks can be reprocessed via the Dashboard or API endpoint <code>POST /api/dashboard/dlq/{'{taskId}'}/reprocess</code>.</li>
              </ul>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

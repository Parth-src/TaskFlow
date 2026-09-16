import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  Cpu,
  Github,
  ArrowRight,
  Zap,
  ShieldCheck,
  Server,
  Layers,
  Activity,
  CheckCircle2,
  XCircle,
  Play,
  RotateCcw,
  Code2,
  FileCode2,
  Sparkles,
  ExternalLink,
  GitBranch,
} from 'lucide-react';
import { Button } from '../components/common/Button';
import { CodeBlock } from '../components/common/CodeBlock';
import { WorkflowGraph } from '../components/workflow/WorkflowGraph';

export const Landing: React.FC<{ onEnterDashboard: () => void }> = ({ onEnterDashboard }) => {
  const { isAuthenticated, loginWithGitHub, devLogin } = useAuth();
  const [activeWorkflowTab, setActiveWorkflowTab] = useState<'parallel' | 'sequential'>('parallel');

  const parallelTasks = [
    { id: 'payment', worker: 'payment', dependsOn: [] },
    { id: 'order', worker: 'order', dependsOn: ['payment'] },
    { id: 'billing', worker: 'billing', dependsOn: ['payment'] },
    { id: 'notification', worker: 'notification', dependsOn: ['order', 'billing'] },
  ];

  const sequentialTasks = [
    { id: 'validate', worker: 'validate', dependsOn: [] },
    { id: 'payment', worker: 'payment', dependsOn: ['validate'] },
    { id: 'inventory', worker: 'inventory', dependsOn: ['payment'] },
    { id: 'fulfillment', worker: 'fulfillment', dependsOn: ['inventory'] },
  ];

  const sampleJsSdk = `import { TaskFlow } from '@taskflow/sdk';

// Initialize TaskFlow client with your project API key
const taskflow = new TaskFlow({
  apiKey: process.env.TASKFLOW_API_KEY,
  endpoint: 'http://localhost:8080'
});

// Define your business logic in an independent worker
taskflow.worker('payment', async (task) => {
  console.log('Processing payment for task:', task.id);
  
  // Real business logic (e.g. Stripe API call)
  const result = await processPayment(task.payload);
  
  return {
    success: true,
    transactionId: result.id
  };
});`;

  const samplePythonSdk = `from taskflow import TaskFlow

# Initialize TaskFlow worker client
taskflow = TaskFlow(
    api_key="tf_live_948f2b3e8c1a",
    endpoint="http://localhost:8080"
)

# Register worker handler
@taskflow.worker("generate-invoice")
def generate_invoice(task):
    print(f"Generating PDF invoice for task: {task.id}")
    pdf_url = render_invoice_pdf(task.data)
    return {"status": "SUCCESS", "invoice_url": pdf_url}`;

  return (
    <div style={{ backgroundColor: 'var(--bg-app)', minHeight: '100vh', color: 'var(--text-primary)' }}>
      {/* Top Navigation */}
      <header className="landing-nav">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div className="logo-icon">
            <Cpu size={18} />
          </div>
          <span style={{ fontSize: '18px', fontWeight: 800, letterSpacing: '-0.02em', color: '#fff' }}>
            TaskFlow
          </span>
        </div>

        <nav style={{ display: 'flex', alignItems: 'center', gap: '28px' }}>
          <a href="#product" style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>Product</a>
          <a href="#how-it-works" style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>How It Works</a>
          <a href="#templates" style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>Templates</a>
          <a href="#sdks" style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>SDKs</a>
          <a href="#docs" style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>Documentation</a>
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {isAuthenticated ? (
            <Button
              variant="primary"
              onClick={onEnterDashboard}
              icon={<ArrowRight size={14} />}
            >
              Open Dashboard
            </Button>
          ) : (
            <>
              <Button
                variant="secondary"
                size="sm"
                onClick={() => devLogin('developer')}
              >
                Local Dev Login
              </Button>
              <Button
                variant="primary"
                onClick={loginWithGitHub}
                icon={<Github size={14} />}
              >
                Login with GitHub
              </Button>
            </>
          )}
        </div>
      </header>

      {/* Hero Section */}
      <section className="hero-section" id="product">
        <div className="hero-badge">
          <Sparkles size={13} />
          <span>Distributed Workflow Orchestration Platform</span>
        </div>

        <h1 className="hero-title">
          Run background work without<br />blocking your APIs.
        </h1>

        <p className="hero-subtitle">
          Orchestrate long-running business workflows asynchronously with dependency-aware execution, distributed workers, automatic retries, and failure handling.
        </p>

        <div className="cta-row">
          {isAuthenticated ? (
            <Button
              variant="primary"
              size="lg"
              onClick={onEnterDashboard}
              icon={<ArrowRight size={16} />}
            >
              Enter TaskFlow Dashboard
            </Button>
          ) : (
            <>
              <Button
                variant="primary"
                size="lg"
                onClick={loginWithGitHub}
                icon={<Github size={16} />}
              >
                Get Started with GitHub
              </Button>
              <Button
                variant="secondary"
                size="lg"
                onClick={() => {
                  const el = document.getElementById('how-it-works');
                  el?.scrollIntoView({ behavior: 'smooth' });
                }}
              >
                See How It Works
              </Button>
            </>
          )}
        </div>
      </section>

      {/* SECTION 6: The Problem vs TaskFlow Solution */}
      <section className="section-container" style={{ paddingTop: 0 }}>
        <div className="section-header">
          <h2 className="section-title">The Synchronous Request Path Problem</h2>
          <p className="section-description">
            When long-running business logic executes inside the request path, API latency increases and the application becomes dependent on every downstream operation completing successfully.
          </p>
        </div>

        <div className="comparison-grid">
          {/* Traditional Synchronous */}
          <div className="card" style={{ border: '1px solid rgba(244, 63, 94, 0.25)', background: '#0b0f17' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--rose-400)', fontWeight: 600, fontSize: '15px', marginBottom: '16px' }}>
              <XCircle size={18} />
              <span>Traditional Synchronous API (High Latency)</span>
            </div>

            <div style={{ background: '#05070c', padding: '16px', borderRadius: '6px', fontFamily: 'var(--font-mono)', fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.8 }}>
              <div style={{ color: 'var(--rose-400)' }}>Client → POST /api/checkout</div>
              <div style={{ paddingLeft: '16px', borderLeft: '2px solid rgba(244, 63, 94, 0.3)' }}>
                <div>├─ Payment Gateway (1.4s)</div>
                <div>├─ Order Processing (2.1s)</div>
                <div>├─ Invoice PDF Generation (3.2s)</div>
                <div>├─ Analytics Ingestion (0.8s)</div>
                <div>└─ Email Dispatch (1.5s)</div>
              </div>
              <div style={{ color: 'var(--rose-400)', marginTop: '8px' }}>
                Total Request Latency: <strong>9.0 seconds</strong> (API Blocked)
              </div>
            </div>

            <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '14px' }}>
              If any downstream step times out or fails, the entire HTTP request fails and the user gets a 504 Gateway Timeout.
            </p>
          </div>

          {/* TaskFlow Asynchronous */}
          <div className="card" style={{ border: '1px solid rgba(6, 182, 212, 0.35)', background: '#0b141e' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--cyan-400)', fontWeight: 600, fontSize: '15px', marginBottom: '16px' }}>
              <CheckCircle2 size={18} />
              <span>TaskFlow Orchestration (Fast Response)</span>
            </div>

            <div style={{ background: '#050a12', padding: '16px', borderRadius: '6px', fontFamily: 'var(--font-mono)', fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.8 }}>
              <div style={{ color: 'var(--emerald-400)' }}>Client → POST /api/checkout</div>
              <div style={{ paddingLeft: '16px', borderLeft: '2px solid var(--cyan-500)' }}>
                <div>└─ taskflow.submit('order-flow')</div>
              </div>
              <div style={{ color: 'var(--emerald-400)', marginTop: '6px' }}>
                ← HTTP 202 Accepted (Execution ID: <span style={{ color: 'var(--cyan-400)' }}>exec_94a2b1</span>) [<strong>18ms</strong>]
              </div>
              <div style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px dashed var(--border-medium)', color: 'var(--text-muted)' }}>
                <div>Background: TaskFlow Engine executes DAG tasks:</div>
                <div style={{ color: 'var(--cyan-400)' }}>Payment → (Order || Invoice || Analytics) → Email</div>
              </div>
            </div>

            <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '14px' }}>
              Your API responds instantly. TaskFlow handles background scheduling, parallel dependency execution, worker dispatch, retries, and DLQ.
            </p>
          </div>
        </div>
      </section>

      {/* SECTION 7: How TaskFlow Works */}
      <section className="section-container" id="how-it-works" style={{ backgroundColor: '#0a0e17', borderTop: '1px solid var(--border-subtle)', borderBottom: '1px solid var(--border-subtle)' }}>
        <div className="section-header">
          <h2 className="section-title">How TaskFlow Works</h2>
          <p className="section-description">
            A distributed orchestration architecture separating execution coordination from business logic.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
          {[
            {
              step: '01',
              title: 'Workflow Submission',
              desc: 'Your application submits a business process definition to TaskFlow API and gets an immediate execution ID.',
              icon: Zap,
              color: 'var(--cyan-400)',
            },
            {
              step: '02',
              title: 'Dependency Resolution',
              desc: 'TaskFlow DAG engine analyzes task prerequisites, identifying which tasks are ready to run sequentially or in parallel.',
              icon: GitBranch,
              color: 'var(--violet-400)',
            },
            {
              step: '03',
              title: 'Redis Runtime State',
              desc: 'Ready tasks are dispatched into Redis queues and scheduled for worker pickup with distributed execution monitoring.',
              icon: Activity,
              color: 'var(--emerald-400)',
            },
            {
              step: '04',
              title: 'Language-Free Dispatch',
              desc: 'HTTP dispatchers call independent workers written in Java, Python, Node.js, Go, or Rust without SDK runtime constraints.',
              icon: Server,
              color: 'var(--amber-400)',
            },
            {
              step: '05',
              title: 'Retries & DLQ',
              desc: 'Transient errors are retried according to policy; exhausted failures are preserved in the Dead Letter Queue for reprocessing.',
              icon: ShieldCheck,
              color: 'var(--rose-400)',
            },
          ].map((item) => {
            const Icon = item.icon;
            return (
              <div key={item.step} className="card" style={{ background: 'var(--bg-surface)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                  <span style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', fontWeight: 700 }}>
                    STAGE {item.step}
                  </span>
                  <div style={{ color: item.color }}>
                    <Icon size={18} />
                  </div>
                </div>
                <h4 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '8px' }}>
                  {item.title}
                </h4>
                <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                  {item.desc}
                </p>
              </div>
            );
          })}
        </div>
      </section>

      {/* SECTION 8: Concrete Workflow DAG Visualizer */}
      <section className="section-container">
        <div className="section-header">
          <h2 className="section-title">Dependency-Aware DAG Execution</h2>
          <p className="section-description">
            Define sequential and parallel dependencies with zero boilerplate. Independent tasks execute concurrently the instant their prerequisites succeed.
          </p>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginBottom: '24px' }}>
          <Button
            variant={activeWorkflowTab === 'parallel' ? 'primary' : 'secondary'}
            onClick={() => setActiveWorkflowTab('parallel')}
            icon={<GitBranch size={14} />}
          >
            Parallel Concurrency (Payment → [Order || Billing] → Notification)
          </Button>
          <Button
            variant={activeWorkflowTab === 'sequential' ? 'primary' : 'secondary'}
            onClick={() => setActiveWorkflowTab('sequential')}
            icon={<Layers size={14} />}
          >
            Sequential Pipeline (Validate → Payment → Inventory → Fulfillment)
          </Button>
        </div>

        <div className="card" style={{ padding: '24px' }}>
          <WorkflowGraph
            tasks={activeWorkflowTab === 'parallel' ? parallelTasks : sequentialTasks}
          />
        </div>
      </section>

      {/* SECTION 9 & 10: Reliability & Language-Independent Workers */}
      <section className="section-container" style={{ backgroundColor: '#0a0e17', borderTop: '1px solid var(--border-subtle)', borderBottom: '1px solid var(--border-subtle)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>
          {/* Reliability & DLQ */}
          <div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--amber-400)', fontSize: '12px', fontWeight: 600, marginBottom: '8px' }}>
              <ShieldCheck size={14} />
              <span>EXECUTION GUARANTEES</span>
            </div>
            <h3 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '12px', color: '#fff' }}>
              Built-in Retry Policies & Dead Letter Queue
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '20px' }}>
              TaskFlow prevents background operations from silently disappearing into logs. If a worker encounters network timeouts or database contention, TaskFlow applies exponential retries before routing exhausted failures to the Dead Letter Queue.
            </p>

            <div style={{ background: '#05080e', padding: '16px', borderRadius: '6px', border: '1px solid var(--border-subtle)', fontSize: '12px', fontFamily: 'var(--font-mono)' }}>
              <div style={{ color: 'var(--cyan-400)' }}>Task Execution → Worker Failure</div>
              <div style={{ paddingLeft: '16px', borderLeft: '2px solid var(--amber-500)', margin: '4px 0' }}>
                <div style={{ color: 'var(--amber-400)' }}>Attempt 1: Failed (Connection reset) → Retry 1</div>
                <div style={{ color: 'var(--amber-400)' }}>Attempt 2: Failed (503 Service Unavailable) → Retry 2</div>
                <div style={{ color: 'var(--amber-400)' }}>Attempt 3: Failed (Timeout) → Retry Exhausted</div>
              </div>
              <div style={{ color: 'var(--rose-400)' }}>
                → Retained in DLQ (TaskFlow Dashboard alert with one-click reprocess)
              </div>
            </div>
          </div>

          {/* Language Independent Workers */}
          <div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--cyan-400)', fontSize: '12px', fontWeight: 600, marginBottom: '8px' }}>
              <Server size={14} />
              <span>POLYGLOT WORKERS</span>
            </div>
            <h3 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '12px', color: '#fff' }}>
              Language-Independent HTTP Workers
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.7, marginBottom: '20px' }}>
              Developers write business logic in their language of choice. TaskFlow coordinates workers through standardized HTTP contracts with bearer authentication, keeping orchestration independent of application frameworks.
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
              <div style={{ background: '#05080e', border: '1px solid var(--border-subtle)', borderRadius: '6px', padding: '12px', textAlign: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: '14px', color: 'var(--cyan-400)' }}>Java / Spring</div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>Port 8081</div>
              </div>
              <div style={{ background: '#05080e', border: '1px solid var(--border-subtle)', borderRadius: '6px', padding: '12px', textAlign: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: '14px', color: 'var(--emerald-400)' }}>Python / FastAPI</div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>Port 8082</div>
              </div>
              <div style={{ background: '#05080e', border: '1px solid var(--border-subtle)', borderRadius: '6px', padding: '12px', textAlign: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: '14px', color: 'var(--violet-400)' }}>Node.js / Express</div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>Port 8083</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* SECTION 12: Real Templates */}
      <section className="section-container" id="templates">
        <div className="section-header">
          <h2 className="section-title">Production Workflow Templates</h2>
          <p className="section-description">
            Start with real, pre-configured workflow pipelines ready for production workloads.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
          {[
            {
              name: 'Payment Processing Pipeline',
              desc: 'Payment capture followed by parallel invoice generation, transactional customer email, and data telemetry.',
              tasks: ['payment', 'send-email', 'generate-invoice', 'analytics'],
            },
            {
              name: 'E-Commerce Order Fulfillment',
              desc: 'Cart validation, payment capture, warehouse inventory locking, and final fulfillment dispatch.',
              tasks: ['validate', 'payment', 'inventory', 'fulfillment'],
            },
            {
              name: 'Omnichannel Notification Pipeline',
              desc: 'Event ingestion, personalized template rendering, multi-channel dispatch, and read receipts tracking.',
              tasks: ['event', 'prepare', 'send', 'track'],
            },
          ].map((template) => (
            <div key={template.name} className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <h4 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '8px' }}>
                  {template.name}
                </h4>
                <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '14px' }}>
                  {template.desc}
                </p>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '16px' }}>
                  {template.tasks.map((task) => (
                    <span
                      key={task}
                      style={{
                        fontSize: '11px',
                        fontFamily: 'var(--font-mono)',
                        padding: '2px 8px',
                        background: 'var(--bg-surface-elevated)',
                        borderRadius: '4px',
                        color: 'var(--cyan-400)',
                        border: '1px solid var(--border-subtle)',
                      }}
                    >
                      {task}
                    </span>
                  ))}
                </div>
              </div>
              <Button
                variant="primary"
                size="sm"
                onClick={onEnterDashboard}
                icon={<Play size={12} />}
              >
                Use in Dashboard
              </Button>
            </div>
          ))}
        </div>
      </section>

      {/* SECTION 13: SDK Integration */}
      <section className="section-container" id="sdks" style={{ backgroundColor: '#0a0e17', borderTop: '1px solid var(--border-subtle)' }}>
        <div className="section-header">
          <h2 className="section-title">Simple Developer Integration</h2>
          <p className="section-description">
            Connect your existing application in minutes using TaskFlow SDKs or lightweight HTTP worker endpoints.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px', color: 'var(--text-secondary)', fontSize: '12px', fontWeight: 600 }}>
              <Code2 size={14} color="var(--cyan-400)" />
              <span>TypeScript / Node.js Integration</span>
            </div>
            <CodeBlock code={sampleJsSdk} language="typescript" title="worker.ts" />
          </div>

          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px', color: 'var(--text-secondary)', fontSize: '12px', fontWeight: 600 }}>
              <FileCode2 size={14} color="var(--emerald-400)" />
              <span>Python Integration</span>
            </div>
            <CodeBlock code={samplePythonSdk} language="python" title="worker.py" />
          </div>
        </div>
      </section>

      {/* SECTION 14: Documentation Cards */}
      <section className="section-container" id="docs">
        <div className="section-header">
          <h2 className="section-title">Developer Documentation</h2>
          <p className="section-description">
            Detailed guides and references for architecting reliable distributed background workflows.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '16px' }}>
          {[
            { title: 'Quick Start Guide', desc: 'Deploy your first background workflow with a local worker in under 5 minutes.' },
            { title: 'Workflow YAML Specification', desc: 'Syntax rules for task dependencies, parallel branches, and timeouts.' },
            { title: 'Worker HTTP Protocol', desc: 'Standardized payload contracts, authorization headers, and status response codes.' },
            { title: 'Retry & Failure Policies', desc: 'Configuring max attempts, backoff multipliers, and Dead Letter Queue retention.' },
            { title: 'API & SDK Reference', desc: 'REST endpoints for programmatic workflow execution and execution querying.' },
            { title: 'Project Scoping & Security', desc: 'Managing project isolation, environment credentials, and API key revocation.' },
          ].map((doc) => (
            <div
              key={doc.title}
              className="card"
              style={{ cursor: 'pointer' }}
              onClick={onEnterDashboard}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <h4 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>
                  {doc.title}
                </h4>
                <ExternalLink size={13} color="var(--text-muted)" />
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                {doc.desc}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer style={{ borderTop: '1px solid var(--border-subtle)', padding: '32px 48px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-muted)', fontSize: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Cpu size={14} color="var(--cyan-400)" />
          <span style={{ color: 'var(--text-secondary)', fontWeight: 600 }}>TaskFlow Platform</span>
          <span>— Distributed Background Workflow Orchestration</span>
        </div>
        <div>
          <span>Connected to Spring Boot & Redis Engine</span>
        </div>
      </footer>
    </div>
  );
};

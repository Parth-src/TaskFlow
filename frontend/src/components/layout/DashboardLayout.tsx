import React, { useState } from 'react';
import { Sidebar } from './Sidebar';
import { TopBar } from './TopBar';
import { TriggerWorkflowModal } from '../workflow/TriggerWorkflowModal';
import { WorkflowSummary } from '../../types';
import { workflowsApi } from '../../api/workflows';

interface DashboardLayoutProps {
  currentPath: string;
  onNavigate: (path: string) => void;
  children: React.ReactNode;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({
  currentPath,
  onNavigate,
  children,
}) => {
  const [quickRunOpen, setQuickRunOpen] = useState(false);
  const [defaultWorkflow, setDefaultWorkflow] = useState<WorkflowSummary | null>(null);

  const handleOpenQuickRun = async () => {
    try {
      const list = await workflowsApi.getWorkflows();
      if (list && list.length > 0) {
        setDefaultWorkflow(list[0]);
        setQuickRunOpen(true);
      }
    } catch (e) {
      console.error('Failed to get workflows for quick run:', e);
    }
  };

  return (
    <div className="app-container">
      <Sidebar currentPath={currentPath} onNavigate={onNavigate} />
      <div className="main-content">
        <TopBar onQuickRun={handleOpenQuickRun} />
        <main className="page-wrapper">{children}</main>
      </div>

      <TriggerWorkflowModal
        isOpen={quickRunOpen}
        onClose={() => setQuickRunOpen(false)}
        workflow={defaultWorkflow}
        onExecutionStarted={(id) => {
          onNavigate(`/dashboard/executions/${id}`);
        }}
      />
    </div>
  );
};

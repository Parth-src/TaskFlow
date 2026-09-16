import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProjectProvider } from './context/ProjectContext';
import { Landing } from './pages/Landing';
import { DashboardLayout } from './components/layout/DashboardLayout';
import { DashboardOverview } from './pages/DashboardOverview';
import { WorkflowsPage } from './pages/WorkflowsPage';
import { ExecutionsPage } from './pages/ExecutionsPage';
import { ExecutionDetailPage } from './pages/ExecutionDetailPage';
import { WorkersPage } from './pages/WorkersPage';
import { DLQPage } from './pages/DLQPage';
import { ProjectsPage } from './pages/ProjectsPage';
import { RepositoriesPage } from './pages/RepositoriesPage';
import { CredentialsPage } from './pages/CredentialsPage';
import { TemplatesPage } from './pages/TemplatesPage';
import { DocumentationPage } from './pages/DocumentationPage';
import { LoadingSpinner } from './components/common/LoadingSpinner';

const parseHashPath = (): string => {
  const hash = window.location.hash.replace(/^#/, '');
  if (hash.startsWith('/dashboard')) {
    return hash;
  }
  const path = window.location.pathname;
  if (path.startsWith('/dashboard')) {
    return path;
  }
  return '/';
};

const MainRouter: React.FC = () => {
  const { isAuthenticated, loading } = useAuth();
  const [currentPath, setCurrentPath] = useState<string>(parseHashPath());

  useEffect(() => {
    const handleNavigation = () => {
      setCurrentPath(parseHashPath());
    };

    window.addEventListener('hashchange', handleNavigation);
    window.addEventListener('popstate', handleNavigation);
    return () => {
      window.removeEventListener('hashchange', handleNavigation);
      window.removeEventListener('popstate', handleNavigation);
    };
  }, []);

  const navigate = (path: string) => {
    window.location.hash = `#${path}`;
    setCurrentPath(path);
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', backgroundColor: 'var(--bg-app)' }}>
        <LoadingSpinner message="Authenticating session with TaskFlow backend..." />
      </div>
    );
  }

  // If user is at root "/" or unauthenticated, render Landing
  if (!currentPath.startsWith('/dashboard')) {
    return <Landing onEnterDashboard={() => navigate('/dashboard')} />;
  }

  // Authentication Guard: if not authenticated, redirect to landing
  if (!isAuthenticated) {
    return <Landing onEnterDashboard={() => navigate('/dashboard')} />;
  }

  // Dashboard Page Routing
  let pageContent: React.ReactNode = null;

  if (currentPath === '/dashboard') {
    pageContent = <DashboardOverview onNavigate={navigate} />;
  } else if (currentPath.startsWith('/dashboard/executions/')) {
    const executionId = currentPath.replace('/dashboard/executions/', '');
    pageContent = <ExecutionDetailPage executionId={executionId} onNavigate={navigate} />;
  } else if (currentPath === '/dashboard/executions') {
    pageContent = <ExecutionsPage onNavigate={navigate} />;
  } else if (currentPath === '/dashboard/workflows') {
    pageContent = <WorkflowsPage onNavigate={navigate} />;
  } else if (currentPath === '/dashboard/workers') {
    pageContent = <WorkersPage />;
  } else if (currentPath === '/dashboard/dlq') {
    pageContent = <DLQPage />;
  } else if (currentPath === '/dashboard/projects') {
    pageContent = <ProjectsPage onNavigate={navigate} />;
  } else if (currentPath === '/dashboard/repositories') {
    pageContent = <RepositoriesPage />;
  } else if (currentPath === '/dashboard/credentials') {
    pageContent = <CredentialsPage />;
  } else if (currentPath === '/dashboard/templates') {
    pageContent = <TemplatesPage onNavigate={navigate} />;
  } else if (currentPath === '/dashboard/documentation') {
    pageContent = <DocumentationPage />;
  } else {
    pageContent = <DashboardOverview onNavigate={navigate} />;
  }

  return (
    <DashboardLayout currentPath={currentPath} onNavigate={navigate}>
      {pageContent}
    </DashboardLayout>
  );
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <ProjectProvider>
        <MainRouter />
      </ProjectProvider>
    </AuthProvider>
  );
};

export default App;

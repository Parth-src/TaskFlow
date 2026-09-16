import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { Project } from '../types';
import { projectsApi } from '../api/projects';
import { useAuth } from './AuthContext';

interface ProjectContextType {
  projects: Project[];
  activeProject: Project | null;
  loading: boolean;
  setActiveProject: (project: Project | null) => void;
  createProject: (name: string) => Promise<Project>;
  refreshProjects: () => Promise<void>;
}

const ProjectContext = createContext<ProjectContextType | undefined>(undefined);

const ACTIVE_PROJECT_KEY = 'taskflow_active_project_id';

export const ProjectProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [projects, setProjects] = useState<Project[]>([]);
  const [activeProject, setActiveProjectState] = useState<Project | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  const refreshProjects = useCallback(async () => {
    if (!isAuthenticated) {
      setProjects([]);
      setActiveProjectState(null);
      return;
    }

    try {
      setLoading(true);
      const list = await projectsApi.getProjects();
      setProjects(list || []);

      const savedId = localStorage.getItem(ACTIVE_PROJECT_KEY);
      if (savedId && list && list.length > 0) {
        const found = list.find((p) => p.id === savedId);
        if (found) {
          setActiveProjectState(found);
          return;
        }
      }

      if (list && list.length > 0) {
        setActiveProjectState(list[0]);
        localStorage.setItem(ACTIVE_PROJECT_KEY, list[0].id);
      } else {
        setActiveProjectState(null);
      }
    } catch (e) {
      console.error('Failed to load projects:', e);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    refreshProjects();
  }, [refreshProjects]);

  const setActiveProject = (project: Project | null) => {
    setActiveProjectState(project);
    if (project) {
      localStorage.setItem(ACTIVE_PROJECT_KEY, project.id);
    } else {
      localStorage.removeItem(ACTIVE_PROJECT_KEY);
    }
  };

  const createProject = async (name: string): Promise<Project> => {
    const created = await projectsApi.createProject(name);
    await refreshProjects();
    setActiveProject(created);
    return created;
  };

  return (
    <ProjectContext.Provider
      value={{
        projects,
        activeProject,
        loading,
        setActiveProject,
        createProject,
        refreshProjects,
      }}
    >
      {children}
    </ProjectContext.Provider>
  );
};

export const useProject = (): ProjectContextType => {
  const context = useContext(ProjectContext);
  if (!context) {
    throw new Error('useProject must be used within a ProjectProvider');
  }
  return context;
};

import React from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  Activity,
  GitBranch,
  PlaySquare,
  Server,
  AlertOctagon,
  FolderGit2,
  KeyRound,
  LayoutTemplate,
  BookOpen,
  LogOut,
  Layers,
  Cpu,
} from 'lucide-react';

interface SidebarProps {
  currentPath: string;
  onNavigate: (path: string) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentPath, onNavigate }) => {
  const { user, logout } = useAuth();

  const navGroups = [
    {
      title: 'OVERVIEW',
      items: [
        { label: 'Overview', path: '/dashboard', icon: Activity },
      ],
    },
    {
      title: 'WORKFLOWS',
      items: [
        { label: 'Workflows', path: '/dashboard/workflows', icon: GitBranch },
        { label: 'Executions', path: '/dashboard/executions', icon: PlaySquare },
      ],
    },
    {
      title: 'INFRASTRUCTURE',
      items: [
        { label: 'Workers', path: '/dashboard/workers', icon: Server },
        { label: 'Dead Letter Queue', path: '/dashboard/dlq', icon: AlertOctagon },
      ],
    },
    {
      title: 'PROJECT',
      items: [
        { label: 'Projects', path: '/dashboard/projects', icon: Layers },
        { label: 'Repositories', path: '/dashboard/repositories', icon: FolderGit2 },
        { label: 'Credentials', path: '/dashboard/credentials', icon: KeyRound },
      ],
    },
    {
      title: 'DEVELOPER',
      items: [
        { label: 'Templates', path: '/dashboard/templates', icon: LayoutTemplate },
        { label: 'Documentation', path: '/dashboard/documentation', icon: BookOpen },
      ],
    },
  ];

  return (
    <aside className="sidebar">
      {/* Brand Header */}
      <div className="sidebar-header">
        <div className="logo-brand">
          <div className="logo-icon">
            <Cpu size={16} />
          </div>
          <span>TaskFlow</span>
        </div>
        <span
          style={{
            fontSize: '10px',
            fontFamily: 'var(--font-mono)',
            padding: '2px 6px',
            background: 'rgba(6, 182, 212, 0.1)',
            border: '1px solid rgba(6, 182, 212, 0.25)',
            color: 'var(--cyan-400)',
            borderRadius: '4px',
          }}
        >
          v0.1
        </span>
      </div>

      {/* Navigation */}
      <nav className="sidebar-nav">
        {navGroups.map((group) => (
          <div key={group.title}>
            <div className="nav-group-title">{group.title}</div>
            <div className="nav-links">
              {group.items.map((item) => {
                const Icon = item.icon;
                const isActive =
                  currentPath === item.path ||
                  (item.path !== '/dashboard' && currentPath.startsWith(item.path));

                return (
                  <button
                    key={item.path}
                    className={`nav-item ${isActive ? 'active' : ''}`}
                    onClick={() => onNavigate(item.path)}
                  >
                    <Icon size={16} />
                    <span>{item.label}</span>
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </nav>

      {/* Footer User Profile */}
      <div className="sidebar-footer">
        <div className="user-profile">
          <div className="user-avatar">
            {user?.username ? user.username.charAt(0).toUpperCase() : 'U'}
          </div>
          <div className="user-info">
            <div className="user-name">{user?.username || 'Authenticated Developer'}</div>
            <div className="user-role font-mono">{user?.githubId || 'GitHub User'}</div>
          </div>
        </div>
        <button
          className="btn-icon"
          title="Sign out"
          onClick={() => logout()}
          style={{ color: 'var(--text-muted)' }}
        >
          <LogOut size={15} />
        </button>
      </div>
    </aside>
  );
};

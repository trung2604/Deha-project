import { Link, useLocation } from 'react-router-dom';
import { Building2, Building, Clock, LayoutDashboard, ReceiptText, UserCircle, Users } from 'lucide-react';
import { useAuth } from '@/features/auth/context/AuthContext';
import { canAccessNavItem } from '@/utils/role';

/**
 * roles: ADMIN | EMPLOYEE — who sees this item in the sidebar
 * (Backend: Users / Departments APIs are ADMIN-only; Profile & auth are for all authenticated users.)
 */
const navItems = [
  { path: '/', label: 'Dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'MANAGER_OFFICE', 'MANAGER_DEPARTMENT', 'EMPLOYEE'] },
  { path: '/attendance', label: 'Attendance', icon: Clock, roles: ['MANAGER_OFFICE', 'MANAGER_DEPARTMENT', 'EMPLOYEE'] },
  { path: '/payroll', label: 'Payroll', icon: ReceiptText, roles: ['ADMIN', 'MANAGER_OFFICE'] },
  { path: '/offices', label: 'Offices', icon: Building, roles: ['ADMIN'] },
  { path: '/users', label: 'Users', icon: Users, roles: ['ADMIN', 'MANAGER_OFFICE'] },
  { path: '/departments', label: 'Departments', icon: Building2, roles: ['ADMIN', 'MANAGER_OFFICE', 'MANAGER_DEPARTMENT'] },
  { path: '/profile', label: 'Profile', icon: UserCircle, roles: ['ADMIN', 'MANAGER_OFFICE', 'MANAGER_DEPARTMENT', 'EMPLOYEE'] },
];

export function Sidebar({ isOpen, onClose }) {
  const location = useLocation();
  const { user } = useAuth();

  return (
    <>
      <aside
        className="hidden lg:flex flex-col w-64 h-full border-r border-white/10"
        style={{
          background:
            'linear-gradient(180deg, #111827 0%, #111827 35%, #0B1220 100%)',
          boxShadow: '8px 0 30px rgba(17, 24, 39, 0.22)',
        }}
      >
        <SidebarContent location={location} onClose={onClose} user={user} />
      </aside>

      <aside
        className={`fixed top-0 left-0 z-40 h-full w-64 transform transition-transform duration-200 lg:hidden ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        style={{
          background:
            'linear-gradient(180deg, #111827 0%, #111827 35%, #0B1220 100%)',
          boxShadow: '8px 0 30px rgba(17, 24, 39, 0.22)',
        }}
      >
        <SidebarContent location={location} onClose={onClose} user={user} />
      </aside>
    </>
  );
}

function SidebarContent({ location, onClose, user }) {
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || 'User';
  const initials = ((user?.firstName?.[0] || '') + (user?.lastName?.[0] || '') || 'U').toUpperCase();
  const roleLabel = formatRoleLabel(user?.role);

  const visibleItems = navItems.filter((item) => canAccessNavItem(item.roles, user?.role));

  return (
    <>
      <div className="h-16 flex items-center px-6 border-b border-white/10">
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full shadow-[0_0_12px_rgba(22,119,255,0.9)]" style={{ backgroundColor: '#1677FF' }} />
          <span
            className="font-semibold tracking-tight"
            style={{ color: '#FFFFFF', fontFamily: 'DM Sans, sans-serif', fontSize: '18px' }}
          >
            HRM System
          </span>
        </div>
      </div>

      <nav className="flex-1 py-6 px-3 overflow-y-auto">
        {visibleItems.map((item) => {
          const isActive =
            item.path === '/'
              ? location.pathname === '/'
              : location.pathname === item.path ||
                (item.path === '/departments' && location.pathname.startsWith('/departments'));
          const Icon = item.icon;

          return (
            <Link
              key={item.path}
              to={item.path}
              onClick={onClose}
              className="flex items-center gap-3 px-3 h-12 mb-1 rounded-xl transition-all duration-200 relative group overflow-hidden"
              style={{
                color: isActive ? '#FFFFFF' : 'rgba(255, 255, 255, 0.65)',
                backgroundColor: isActive ? 'rgba(22, 119, 255, 0.16)' : 'transparent',
                boxShadow: isActive ? 'inset 0 0 0 1px rgba(22,119,255,0.24)' : 'none',
              }}
            >
              {isActive && (
                <div
                  className="absolute left-0 top-0 bottom-0 w-[3px] rounded-r"
                  style={{ backgroundColor: '#1677FF' }}
                />
              )}
              <Icon className={`w-5 h-5 shrink-0 transition-transform duration-200 ${isActive ? 'scale-105' : 'group-hover:scale-105'}`} />
              <span className="font-medium" style={{ fontSize: '14px' }}>
                {item.label}
              </span>
              {!isActive && (
                <div
                  className="absolute inset-0 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-150"
                  style={{ backgroundColor: 'rgba(255, 255, 255, 0.05)' }}
                />
              )}
            </Link>
          );
        })}
      </nav>

      <div className="p-4 border-t border-white/10">
        <div className="flex items-center gap-3 px-2">
          <div
            className="w-8 h-8 rounded-full flex items-center justify-center text-white"
            style={{ backgroundColor: '#1677FF', fontSize: '12px', fontWeight: '600' }}
          >
            {initials}
          </div>
          <div className="flex-1 min-w-0">
            <div className="text-white font-medium truncate" style={{ fontSize: '14px' }}>
              {fullName}
            </div>
            <div className="text-white/50 truncate" style={{ fontSize: '12px' }}>
              {roleLabel}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

function formatRoleLabel(role) {
  if (!role) return 'Employee';
  const s = String(role);
  if (s.includes('ADMIN')) return 'Administrator';
  if (s.includes('MANAGER_DEPARTMENT')) return 'Department Manager';
  if (s.includes('MANAGER_OFFICE')) return 'Office Manager';
  if (s.includes('MANAGER')) return 'Manager'; // legacy
  if (s.includes('EMPLOYEE')) return 'Employee';
  return s.replace(/^ROLE_/, '');
}

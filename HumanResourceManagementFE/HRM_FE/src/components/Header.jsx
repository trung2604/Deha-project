import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  AlertCircle,
  Bell,
  Building2,
  Calendar,
  CheckCircle,
  ChevronDown,
  Clock,
  LogOut,
  Menu,
  Search,
  User,
  Users,
  XCircle,
} from 'lucide-react';
import { toast } from 'sonner';
import { useAuth } from '@/features/auth/context/AuthContext';
import { Input } from 'antd';

const breadcrumbMap = {
  '/': ['Dashboard'],
  '/users': ['Users'],
  '/offices': ['Offices'],
  '/departments': ['Departments'],
  '/attendance': ['Attendance'],
  '/payroll': ['Payroll'],
  '/leave-requests': ['Leave Requests'],
  '/salary': ['Salary'],
  '/activity-logs': ['Activity Logs'],
  '/profile': ['Profile'],
  '/notifications': ['Notifications'],
};

const notificationIcons = {
  success: CheckCircle,
  warning: AlertCircle,
  error: XCircle,
  info: Clock,
};

const notificationColors = {
  success: { bg: 'rgba(82, 196, 26, 0.1)', text: '#52C41A' },
  warning: { bg: 'rgba(250, 140, 22, 0.1)', text: '#FA8C16' },
  error: { bg: 'rgba(255, 77, 79, 0.1)', text: '#FF4D4F' },
  info: { bg: 'rgba(22, 119, 255, 0.1)', text: '#1677FF' },
};

export function Header({ onMenuClick }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [displayedCount, setDisplayedCount] = useState(5);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const notificationListRef = useRef(null);

  const breadcrumbs = breadcrumbMap[location.pathname] || ['Dashboard'];
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || 'User';
  const initials = (user?.firstName?.[0] || '') + (user?.lastName?.[0] || '') || 'U';

  const unreadCount = notifications.filter((n) => !n.read).length;
  const displayedNotifications = notifications.slice(0, displayedCount);
  const hasMore = displayedCount < notifications.length;

  const handleNotificationScroll = (e) => {
    const element = e.target;
    const isNearBottom = element.scrollHeight - element.scrollTop - element.clientHeight < 50;

    if (isNearBottom && hasMore && !isLoadingMore) {
      setIsLoadingMore(true);
      setTimeout(() => {
        setDisplayedCount((prev) => Math.min(prev + 5, notifications.length));
        setIsLoadingMore(false);
      }, 500);
    }
  };

  useEffect(() => {
    if (!notificationOpen) return;
    return () => setDisplayedCount(5);
  }, [notificationOpen]);

  const searchResults = { users: [], departments: [], leaveRequests: [] };
  const totalResults = 0;

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    setSearchOpen(e.target.value.length > 0);
  };

  const handleResultClick = (path) => {
    navigate(path);
    setSearchOpen(false);
    setSearchTerm('');
  };

  const markAsRead = (id) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
  };

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const handleLogout = () => {
    logout();
    setDropdownOpen(false);
    toast.success('Logged out successfully');
    navigate('/login', { replace: true });
  };

  return (
    <header
      className="h-16 border-b flex items-center justify-between px-4 md:px-6 sticky top-0 z-20 backdrop-blur-md"
      style={{
        backgroundColor: 'rgba(255,255,255,0.82)',
        borderColor: 'rgba(232,232,232,0.9)',
        boxShadow: '0 8px 20px rgba(15, 23, 42, 0.04)',
      }}
    >
      <div className="flex items-center gap-4">
        <button
          onClick={onMenuClick}
          className="lg:hidden p-2 hover:bg-gray-100 rounded-lg transition-colors"
          style={{ color: '#0A0A0A' }}
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-2 text-sm">
          {breadcrumbs.map((crumb, index) => (
            <div key={index} className="flex items-center gap-2">
              {index > 0 && <span className="text-gray-400">/</span>}
              <span
                className={index === breadcrumbs.length - 1 ? '' : 'text-gray-500'}
                style={{
                  color: index === breadcrumbs.length - 1 ? '#0A0A0A' : '#595959',
                  fontWeight: index === breadcrumbs.length - 1 ? '500' : '400',
                }}
              >
                {crumb}
              </span>
            </div>
          ))}
        </div>
      </div>

      <div className="hidden md:flex items-center flex-1 max-w-md mx-auto relative">
        <div className="relative w-full">
          <Input
            type="text"
            placeholder="Search users, departments, leave requests..."
            className="w-full h-9 pr-4 rounded-xl border-0!"
            style={{
              backgroundColor: 'rgba(15,23,42,0.04)',
              color: '#0A0A0A',
              boxShadow: 'inset 0 0 0 1px rgba(15,23,42,0.06)',
            }}
            value={searchTerm}
            onChange={handleSearchChange}
            prefix={<Search className="w-4 h-4" style={{ color: '#595959' }} />}
            size="middle"
          />
        </div>

        {searchOpen && totalResults > 0 && (
          <>
            <div className="fixed inset-0 z-10" onClick={() => setSearchOpen(false)} />
            <div
              className="absolute left-0 right-0 top-full mt-2 rounded-lg shadow-lg border overflow-hidden z-20"
              style={{ backgroundColor: '#FFFFFF', borderColor: '#E8E8E8' }}
            >
              <div className="px-4 py-3 border-b" style={{ borderColor: '#E8E8E8' }}>
                <h3 style={{ fontFamily: 'DM Sans, sans-serif', fontSize: '14px', fontWeight: '600', color: '#0A0A0A' }}>
                  Search Results
                </h3>
                <p style={{ color: '#595959', fontSize: '12px' }}>
                  {totalResults} result{totalResults > 1 ? 's' : ''} found
                </p>
              </div>
              <div className="max-h-96 overflow-y-auto">
                {searchResults.users.length > 0 && (
                  <div>
                    <div className="px-4 py-2" style={{ backgroundColor: '#F5F7FA' }}>
                      <p style={{ color: '#595959', fontSize: '11px', fontWeight: '600', textTransform: 'uppercase' }}>
                        Users ({searchResults.users.length})
                      </p>
                    </div>
                    {searchResults.users.map((user) => (
                      <div
                        key={user.id}
                        onClick={() => handleResultClick('/users')}
                        className="px-4 py-3 border-b hover:bg-gray-50 transition-colors cursor-pointer"
                        style={{ borderColor: '#E8E8E8' }}
                      >
                        <div className="flex gap-3 items-center">
                          <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0" style={{ backgroundColor: 'rgba(22, 119, 255, 0.1)' }}>
                            <Users className="w-4 h-4" style={{ color: '#1677FF' }} />
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 style={{ fontSize: '13px', fontWeight: '600', color: '#0A0A0A' }}>{user.name}</h4>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {searchResults.departments.length > 0 && (
                  <div>
                    <div className="px-4 py-2" style={{ backgroundColor: '#F5F7FA' }}>
                      <p style={{ color: '#595959', fontSize: '11px', fontWeight: '600', textTransform: 'uppercase' }}>
                        Departments ({searchResults.departments.length})
                      </p>
                    </div>
                    {searchResults.departments.map((dept) => (
                      <div
                        key={dept.id}
                        onClick={() => handleResultClick('/departments')}
                        className="px-4 py-3 border-b hover:bg-gray-50 transition-colors cursor-pointer"
                        style={{ borderColor: '#E8E8E8' }}
                      >
                        <div className="flex gap-3 items-center">
                          <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0" style={{ backgroundColor: 'rgba(22, 119, 255, 0.1)' }}>
                            <Building2 className="w-4 h-4" style={{ color: '#1677FF' }} />
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 style={{ fontSize: '13px', fontWeight: '600', color: '#0A0A0A' }}>{dept.name}</h4>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {searchResults.leaveRequests.length > 0 && (
                  <div>
                    <div className="px-4 py-2" style={{ backgroundColor: '#F5F7FA' }}>
                      <p style={{ color: '#595959', fontSize: '11px', fontWeight: '600', textTransform: 'uppercase' }}>
                        Leave Requests ({searchResults.leaveRequests.length})
                      </p>
                    </div>
                    {searchResults.leaveRequests.map((req) => (
                      <div
                        key={req.id}
                        onClick={() => handleResultClick('/leave-requests')}
                        className="px-4 py-3 border-b hover:bg-gray-50 transition-colors cursor-pointer"
                        style={{ borderColor: '#E8E8E8' }}
                      >
                        <div className="flex gap-3 items-center">
                          <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0" style={{ backgroundColor: 'rgba(22, 119, 255, 0.1)' }}>
                            <Calendar className="w-4 h-4" style={{ color: '#1677FF' }} />
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 style={{ fontSize: '13px', fontWeight: '600', color: '#0A0A0A' }}>{req.user}</h4>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </>
        )}

        {searchOpen && totalResults === 0 && searchTerm.length > 0 && (
          <>
            <div className="fixed inset-0 z-10" onClick={() => setSearchOpen(false)} />
            <div
              className="absolute left-0 right-0 top-full mt-2 rounded-lg shadow-lg border overflow-hidden z-20"
              style={{ backgroundColor: '#FFFFFF', borderColor: '#E8E8E8' }}
            >
              <div className="px-4 py-8 text-center">
                <Search className="w-12 h-12 mx-auto mb-2" style={{ color: '#E8E8E8' }} />
                <p style={{ color: '#595959', fontSize: '14px' }}>No results found for "{searchTerm}"</p>
              </div>
            </div>
          </>
        )}
      </div>

      <div className="flex items-center gap-4">
        <div className="relative">
          <button
            onClick={() => setNotificationOpen(!notificationOpen)}
            className="relative p-2 hover:bg-gray-100 rounded-xl transition-colors"
            style={{ color: '#0A0A0A' }}
          >
            <Bell className="w-5 h-5" />
            {unreadCount > 0 && <span className="absolute top-1 right-1 w-2 h-2 rounded-full" style={{ backgroundColor: '#FF4D4F' }} />}
          </button>

          {notificationOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setNotificationOpen(false)} />
              <div
                className="absolute right-0 top-full mt-2 w-96 rounded-lg shadow-lg border overflow-hidden z-20"
                style={{ backgroundColor: '#FFFFFF', borderColor: '#E8E8E8' }}
              >
                <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: '#E8E8E8' }}>
                  <div>
                    <h3 style={{ fontFamily: 'DM Sans, sans-serif', fontSize: '14px', fontWeight: '600', color: '#0A0A0A' }}>
                      Notifications
                    </h3>
                    {unreadCount > 0 && <p style={{ color: '#595959', fontSize: '12px' }}>{unreadCount} unread</p>}
                  </div>
                  {unreadCount > 0 && (
                    <button onClick={markAllAsRead} style={{ color: '#1677FF', fontSize: '12px', fontWeight: '500' }} className="hover:underline">
                      Mark all as read
                    </button>
                  )}
                </div>

                <div className="max-h-96 overflow-y-auto" ref={notificationListRef} onScroll={handleNotificationScroll}>
                  {notifications.length === 0 ? (
                    <div className="px-4 py-8 text-center">
                      <Bell className="w-12 h-12 mx-auto mb-2" style={{ color: '#E8E8E8' }} />
                      <p style={{ color: '#595959', fontSize: '14px' }}>No notifications</p>
                    </div>
                  ) : (
                    <>
                      {displayedNotifications.map((notification) => {
                        const Icon = notificationIcons[notification.type];
                        const colors = notificationColors[notification.type];

                        return (
                          <div
                            key={notification.id}
                            onClick={() => markAsRead(notification.id)}
                            className="px-4 py-3 border-b hover:bg-gray-50 transition-colors cursor-pointer"
                            style={{
                              borderColor: '#E8E8E8',
                              backgroundColor: notification.read ? 'transparent' : 'rgba(22, 119, 255, 0.02)',
                            }}
                          >
                            <div className="flex gap-3">
                              <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0" style={{ backgroundColor: colors.bg }}>
                                <Icon className="w-4 h-4" style={{ color: colors.text }} />
                              </div>
                              <div className="flex-1 min-w-0">
                                <div className="flex items-start justify-between gap-2 mb-1">
                                  <h4 style={{ fontSize: '13px', fontWeight: notification.read ? '500' : '600', color: '#0A0A0A' }}>
                                    {notification.title}
                                  </h4>
                                  {!notification.read && <div className="w-2 h-2 rounded-full shrink-0 mt-1" style={{ backgroundColor: '#1677FF' }} />}
                                </div>
                                <p className="mb-1" style={{ color: '#595959', fontSize: '12px', lineHeight: '1.4' }}>
                                  {notification.message}
                                </p>
                                <span style={{ color: '#8C8C8C', fontSize: '11px', fontFamily: 'JetBrains Mono, monospace' }}>
                                  {notification.time}
                                </span>
                              </div>
                            </div>
                          </div>
                        );
                      })}

                      {isLoadingMore && (
                        <div className="px-4 py-3 text-center">
                          <div className="w-5 h-5 border-2 border-t-transparent rounded-full animate-spin mx-auto" style={{ borderColor: '#1677FF', borderTopColor: 'transparent' }} />
                        </div>
                      )}
                    </>
                  )}
                </div>

                {notifications.length > 0 && (
                  <div className="px-4 py-3 border-t text-center" style={{ borderColor: '#E8E8E8' }}>
                    <button
                      onClick={() => {
                        setNotificationOpen(false);
                        navigate('/notifications');
                      }}
                      style={{ color: '#1677FF', fontSize: '13px', fontWeight: '500' }}
                      className="hover:underline"
                    >
                      View all notifications
                    </button>
                  </div>
                )}
              </div>
            </>
          )}
        </div>

        <div className="w-px h-6" style={{ backgroundColor: '#E8E8E8' }} />

        <div className="relative">
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center gap-2 p-2 hover:bg-gray-100 rounded-xl transition-colors"
          >
            <div className="w-8 h-8 rounded-full flex items-center justify-center text-white" style={{ backgroundColor: '#1677FF', fontSize: '12px', fontWeight: '600' }}>
              {initials.toUpperCase()}
            </div>
            <span className="hidden sm:block font-medium" style={{ color: '#0A0A0A', fontSize: '14px' }}>
              {fullName}
            </span>
            <ChevronDown className="w-4 h-4" style={{ color: '#595959' }} />
          </button>

          {dropdownOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setDropdownOpen(false)} />
              <div className="absolute right-0 top-full mt-2 w-48 rounded-lg shadow-lg border overflow-hidden z-20" style={{ backgroundColor: '#FFFFFF', borderColor: '#E8E8E8' }}>
                <button
                  onClick={() => {
                    setDropdownOpen(false);
                    navigate('/profile');
                  }}
                  className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-50 transition-colors text-left"
                >
                  <User className="w-4 h-4" style={{ color: '#595959' }} />
                  <span style={{ color: '#0A0A0A', fontSize: '14px' }}>Profile</span>
                </button>
                <div className="border-t" style={{ borderColor: '#E8E8E8' }} />
                <button className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-50 transition-colors text-left" onClick={handleLogout}>
                  <LogOut className="w-4 h-4" style={{ color: '#FF4D4F' }} />
                  <span style={{ color: '#FF4D4F', fontSize: '14px' }}>Logout</span>
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  );
}


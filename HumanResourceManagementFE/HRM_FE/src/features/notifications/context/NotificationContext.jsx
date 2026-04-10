/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import notificationService from "@/features/notifications/api/notificationService";
import { isSuccessResponse } from "@/utils/apiResponse";
import { createRealtimeClient } from "@/utils/realtimeClient";
import { useAuth } from "@/features/auth/context/AuthContext";

const NotificationContext = createContext(null);

function toBooleanRead(notification) {
  if (typeof notification?.read === "boolean") return notification.read;
  if (typeof notification?.isRead === "boolean") return notification.isRead;
  return false;
}

function normalizeNotification(notification) {
  return {
    id: notification?.id,
    title: notification?.title || "Notification",
    body: notification?.body || "",
    type: String(notification?.type || "SYSTEM").toUpperCase(),
    referenceId: notification?.referenceId || "",
    createdAt: notification?.createdAt || null,
    read: toBooleanRead(notification),
  };
}

function mergeById(baseItems, incomingItems, prepend = false) {
  const incoming = Array.isArray(incomingItems) ? incomingItems.filter((item) => item?.id) : [];
  const existing = Array.isArray(baseItems) ? baseItems : [];

  const map = new Map();
  const ordered = prepend ? [...incoming, ...existing] : [...existing, ...incoming];

  for (const item of ordered) {
    map.set(String(item.id), item);
  }

  if (prepend) {
    return [...map.values()];
  }

  const seen = new Set();
  const result = [];
  for (const item of ordered) {
    const key = String(item.id);
    if (seen.has(key)) continue;
    seen.add(key);
    result.push(map.get(key));
  }
  return result;
}

export function NotificationProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const realtimeClientRef = useRef(null);
  const realtimeSubscriptionRef = useRef(null);

  const fetchNotifications = useCallback(async ({ page = 0, size = 50, append = false } = {}) => {
    setLoading(true);
    try {
      const res = await notificationService.getMyNotifications({ page, size });
      if (!isSuccessResponse(res)) {
        return { ok: false, response: res };
      }

      const normalized = Array.isArray(res?.data) ? res.data.map(normalizeNotification) : [];
      setNotifications((prev) => (append ? mergeById(prev, normalized, false) : normalized));
      return { ok: true, response: res, data: normalized };
    } catch {
      return { ok: false, response: null };
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshNotifications = useCallback(async () => {
    return fetchNotifications({ page: 0, size: 50, append: false });
  }, [fetchNotifications]);

  const loadMoreNotifications = useCallback(async ({ page, size = 20 } = {}) => {
    return fetchNotifications({ page, size, append: true });
  }, [fetchNotifications]);

  const markNotificationRead = useCallback(async (id) => {
    if (!id) return { ok: false, response: null };

    const target = notifications.find((item) => String(item.id) === String(id));
    if (target?.read) return { ok: true, response: null };

    try {
      const res = await notificationService.markAsRead(id);
      if (!isSuccessResponse(res)) {
        return { ok: false, response: res };
      }
      setNotifications((prev) => prev.map((item) => (String(item.id) === String(id) ? { ...item, read: true } : item)));
      return { ok: true, response: res };
    } catch {
      return { ok: false, response: null };
    }
  }, [notifications]);

  const markAllNotificationsRead = useCallback(async () => {
    try {
      const res = await notificationService.markAllAsRead();
      if (!isSuccessResponse(res)) {
        return { ok: false, response: res };
      }
      setNotifications((prev) => prev.map((item) => ({ ...item, read: true })));
      return { ok: true, response: res };
    } catch {
      return { ok: false, response: null };
    }
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      setNotifications([]);
      return undefined;
    }

    refreshNotifications();
    const timer = setInterval(() => {
      refreshNotifications();
    }, 30000);
    return () => clearInterval(timer);
  }, [isAuthenticated, refreshNotifications]);

  useEffect(() => {
    if (!isAuthenticated) return undefined;

    const token = localStorage.getItem("auth_token");
    if (!token) return undefined;

    const client = createRealtimeClient({
      token,
      onConnect: (activeClient) => {
        realtimeSubscriptionRef.current?.unsubscribe();
        realtimeSubscriptionRef.current = activeClient.subscribe("/user/queue/notify", (frame) => {
          try {
            const payload = JSON.parse(frame.body || "{}");
            const normalized = normalizeNotification(payload);
            setNotifications((prev) => mergeById(prev, [normalized], true));
          } catch {
            // Ignore malformed websocket payloads.
          }
        });
      },
    });

    realtimeClientRef.current = client;
    client.activate();

    return () => {
      realtimeSubscriptionRef.current?.unsubscribe();
      realtimeSubscriptionRef.current = null;
      realtimeClientRef.current = null;
      client.deactivate();
    };
  }, [isAuthenticated]);

  const unreadCount = useMemo(
    () => notifications.filter((notification) => !notification.read).length,
    [notifications],
  );

  const value = useMemo(
    () => ({
      notifications,
      unreadCount,
      isNotificationsLoading: loading,
      refreshNotifications,
      loadMoreNotifications,
      markNotificationRead,
      markAllNotificationsRead,
    }),
    [
      notifications,
      unreadCount,
      loading,
      refreshNotifications,
      loadMoreNotifications,
      markNotificationRead,
      markAllNotificationsRead,
    ],
  );

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
}

export function useNotifications() {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error("useNotifications must be used within NotificationProvider");
  }
  return context;
}


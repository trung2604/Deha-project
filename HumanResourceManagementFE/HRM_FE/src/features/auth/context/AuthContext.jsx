/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import authService from "../api/authService";
import { getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";

const AuthContext = createContext(null);

const TOKEN_KEY = "auth_token";
const USER_KEY = "user_info";

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(() => readStoredUser());
  const [initializing, setInitializing] = useState(true);

  const clearAuth = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
  }, []);

  const refreshProfile = useCallback(async () => {
    try {
      const res = await authService.getMe();
      if (!isSuccessResponse(res)) {
        const statusCode = Number(res?.statusCode ?? 0);
        if (statusCode === 401) {
          clearAuth();
        }
        return { ok: false, message: getResponseMessage(res, "Unable to refresh profile") };
      }

      const profile = res.data ?? null;
      setUser(profile);
      localStorage.setItem(USER_KEY, JSON.stringify(profile));
      return { ok: true, data: profile, message: res.message };
    } catch {
      return { ok: false, message: "Unable to refresh profile" };
    }
  }, [clearAuth]);

  const updateProfile = useCallback(
    async (payload) => {
      try {
        const res = await authService.updateMe(payload);
        if (!isSuccessResponse(res)) {
          return { ok: false, message: getResponseMessage(res, "Update profile failed") };
        }

        const profile = res.data ?? null;
        setUser(profile);
        localStorage.setItem(USER_KEY, JSON.stringify(profile));
        return { ok: true, data: profile, message: res.message || "Profile updated successfully" };
      } catch {
        return { ok: false, message: "Update profile failed" };
      }
    },
    [],
  );

  const login = useCallback(
    async (credentials) => {
      try {
        const res = await authService.login(credentials);
        if (!isSuccessResponse(res)) {
          return { ok: false, message: getResponseMessage(res, "Login failed") };
        }

        const nextToken = res.data?.token;
        if (!nextToken) {
          return { ok: false, message: "Token is missing in response" };
        }

        localStorage.setItem(TOKEN_KEY, nextToken);
        setToken(nextToken);

        const profileResult = await refreshProfile();
        if (!profileResult.ok) {
          return profileResult;
        }

        return { ok: true, data: profileResult.data, message: res.message || "Login successful" };
      } catch (e) {
        const msg =
          e?.response?.data?.message ||
          e?.message ||
          "Unable to reach the server. Please check your connection.";
        return { ok: false, message: msg };
      }
    },
    [refreshProfile],
  );

  const logout = useCallback(() => {
    authService.logout();
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    let mounted = true;

    const bootstrap = async () => {
      if (!token) {
        if (mounted) setInitializing(false);
        return;
      }

      await refreshProfile();
      if (mounted) setInitializing(false);
    };

    bootstrap();
    return () => {
      mounted = false;
    };
  }, [token, refreshProfile]);

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      initializing,
      login,
      logout,
      refreshProfile,
      updateProfile,
    }),
    [token, user, initializing, login, logout, refreshProfile, updateProfile],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

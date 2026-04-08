import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8081/api";

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

const refreshClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

let refreshTokenPromise = null;
let isRefreshing = false;
let pendingRequests = [];

function clearSessionAndRedirect() {
  localStorage.removeItem("auth_token");
  localStorage.removeItem("user_info");
  window.location.href = "/login";
}

function subscribeTokenRefresh() {
  return new Promise((resolve, reject) => {
    pendingRequests.push({ resolve, reject });
  });
}

function flushPendingRequests(error, token) {
  pendingRequests.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
      return;
    }
    resolve(token);
  });
  pendingRequests = [];
}

async function refreshAccessToken() {
  const refreshRes = await refreshClient.post("/auth/refresh", null, {
    withCredentials: true,
    _skipAuthRefresh: true,
  });

  const nextToken = refreshRes?.data?.data?.token;
  if (!nextToken) {
    throw new Error("Missing token in refresh response");
  }
  localStorage.setItem("auth_token", nextToken);
  return nextToken;
}

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("auth_token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error),
);

axiosInstance.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    // IMPORTANT:
    // - For backend errors (HTTP 4xx/5xx) we still return ApiResponse body
    //   so the UI can check `res.status` and show `res.message`.
    // - For network/timeout errors we reject because there's no ApiResponse.
    if (error.code === "ECONNABORTED" || !error.response) {
      return Promise.reject(error);
    }

    const status = error.response?.status;
    const originalRequest = error.config;

    // Only refresh on 401 (expired/invalid access token).
    // 403 is usually authorization failure and should not force logout.
    if (status === 401 && originalRequest && !originalRequest._skipAuthRefresh) {
      if (originalRequest._retry) {
        clearSessionAndRedirect();
        return error.response?.data;
      }

      originalRequest._retry = true;

      if (isRefreshing) {
        try {
          const queuedToken = await subscribeTokenRefresh();
          originalRequest.headers = originalRequest.headers || {};
          originalRequest.headers.Authorization = `Bearer ${queuedToken}`;
          return axiosInstance(originalRequest);
        } catch {
          clearSessionAndRedirect();
          return error.response?.data;
        }
      }

      isRefreshing = true;

      try {
        if (!refreshTokenPromise) {
          refreshTokenPromise = refreshAccessToken();
        }

        const nextToken = await refreshTokenPromise;
        if (!nextToken) {
          throw new Error("Missing token in refresh response");
        }

        flushPendingRequests(null, nextToken);

        // retry original request with updated token
        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${nextToken}`;
        return axiosInstance(originalRequest);
      } catch {
        flushPendingRequests(new Error("Refresh failed"), null);
        clearSessionAndRedirect();
        return error.response?.data;
      } finally {
        isRefreshing = false;
        refreshTokenPromise = null;
      }
    }

    return error.response?.data;
  },
);

export default axiosInstance;

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

    // Some secured endpoints may return 403 (AccessDenied) instead of 401 (Unauthorized)
    // when the JWT is missing/expired/invalid. Refresh in both cases.
    if ((status === 401 || status === 403) && originalRequest && !originalRequest._skipAuthRefresh) {
      if (originalRequest._retry) {
        localStorage.removeItem("auth_token");
        localStorage.removeItem("user_info");
        window.location.href = "/login";
        return error.response?.data;
      }

      originalRequest._retry = true;

      try {
        const refreshRes = await axiosInstance.post(
          "/auth/refresh",
          null,
          {
            withCredentials: true,
            _skipAuthRefresh: true,
          },
        );

        const nextToken = refreshRes?.data?.token;
        if (!nextToken) throw new Error("Missing token in refresh response");

        localStorage.setItem("auth_token", nextToken);
        // retry original request with updated token
        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${nextToken}`;
        return axiosInstance(originalRequest);
      } catch {
        localStorage.removeItem("auth_token");
        localStorage.removeItem("user_info");
        window.location.href = "/login";
        return error.response?.data;
      }
    }

    return error.response?.data;
  },
);

export default axiosInstance;

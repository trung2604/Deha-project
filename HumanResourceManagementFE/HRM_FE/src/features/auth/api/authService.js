import axios, { API_ORIGIN } from "@/utils/axios";

const authService = {
  login: async (credentials) => {
    // 401 on login must NOT trigger refresh-token flow + redirect (would hide the error toast)
    return await axios.post("/auth/login", credentials, {
      _skipAuthRefresh: true,
      _skipAuthHeader: true,
    });
  },

  exchangeOAuth2Code: async (code) => {
    return await axios.post("/auth/oauth2/exchange", null, {
      params: { code },
      withCredentials: true,
      _skipAuthRefresh: true,
      _skipAuthHeader: true,
    });
  },

  getGoogleLoginUrl: () => `${API_ORIGIN}/oauth2/authorization/google`,

  getMe: async () => {
    return await axios.get("/auth/me");
  },

  verifyEmail: async (token) => {
    return await axios.get("/auth/verify", {
      params: { token },
      _skipAuthRefresh: true,
      _skipAuthHeader: true,
    });
  },

  forgotPassword: async (email) => {
    return await axios.post("/auth/forgot-password", { email }, {
      _skipAuthRefresh: true,
      _skipAuthHeader: true,
    });
  },

  verifyOtp: async ({ email, otp }) => {
    return await axios.post("/auth/verify-otp", { email, otp }, {
      _skipAuthRefresh: true,
      _skipAuthHeader: true,
    });
  },

  resetPassword: async ({ resetToken, newPassword }) => {
    return await axios.post("/auth/reset-password", { resetToken, newPassword }, {
      _skipAuthRefresh: true,
      _skipAuthHeader: true,
    });
  },

  changePassword: async ({ currentPassword, newPassword }) => {
    return await axios.put("/auth/change-password", { currentPassword, newPassword });
  },

  uploadAvatar: async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    return await axios.post("/users/me/avatar", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  removeAvatar: async () => {
    return await axios.delete("/users/me/avatar");
  },

  updateMe: async (payload) => {
    return await axios.put("/auth/me", payload);
  },

  logout: async () => {
    try {
      await axios.post("/auth/logout", null, { withCredentials: true });
    } catch (error) {
      console.error("Logout failed:", error);
    } finally {
      localStorage.removeItem("auth_token");
      localStorage.removeItem("user_info");
    }
  },
};

export default authService;

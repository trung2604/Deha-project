import axios from "@/utils/axios";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081/api";
const API_ORIGIN = API_URL.replace(/\/api\/?$/, "");

const authService = {
  login: async (credentials) => {
    // 401 on login must NOT trigger refresh-token flow + redirect (would hide the error toast)
    return await axios.post("/auth/login", credentials, { _skipAuthRefresh: true });
  },

  exchangeOAuth2Code: async (code) => {
    return await axios.post("/auth/oauth2/exchange", null, {
      params: { code },
      withCredentials: true,
      _skipAuthRefresh: true,
    });
  },

  getGoogleLoginUrl: () => `${API_ORIGIN}/oauth2/authorization/google`,

  getMe: async () => {
    return await axios.get("/auth/me");
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

import axios from "@/utils/axios";

const authService = {
  login: async (credentials) => {
    // 401 on login must NOT trigger refresh-token flow + redirect (would hide the error toast)
    return await axios.post("/auth/login", credentials, { _skipAuthRefresh: true });
  },

  getMe: async () => {
    return await axios.get("/auth/me");
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

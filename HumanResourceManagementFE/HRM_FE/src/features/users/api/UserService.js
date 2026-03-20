import axios from "@/utils/axios";

const UserService = {
  getUsers: async ({ page = 0, size = 10 } = {}) => {
    return await axios.get("/users", { params: { page, size } });
  },

  getUser: async (id) => {
    return await axios.get(`/users/${id}`);
  },

  createUser: async (userData) => {
    return await axios.post("/users", userData);
  },

  updateUser: async (id, userData) => {
    return await axios.put(`/users/${id}`, userData);
  },

  deleteUser: async (id) => {
    return await axios.delete(`/users/${id}`);
  },

  searchUsers: async ({ keyword, page = 0, size = 10 } = {}) => {
    return await axios.get("/users/search", {
      params: { keyword, page, size },
    });
  },

  exportUsers: async (format = "csv") => {
    return await axios.get("/users/export", {
      params: { format },
      responseType: "blob",
    });
  },
};

export default UserService;

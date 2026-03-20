import axios from "@/utils/axios";

const UserService = {
  getUsers: async (params = {}) => {
    return await axios.get("/users", { params });
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

  searchUsers: async (searchTerm) => {
    return await axios.get("/users/search", {
      params: { q: searchTerm },
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

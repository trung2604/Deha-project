import axios from "@/utils/axios";

const UserService = {
  getUsers: async ({ page = 0, size = 10, keyword, officeId, departmentId, positionId, active } = {}) => {
    return await axios.get("/users", {
      params: { page, size, keyword, officeId, departmentId, positionId, active },
    });
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

  deactivateUser: async (id) => {
    return await axios.patch(`/users/${id}/deactivate`);
  },

  resetUserPassword: async (id, newPassword) => {
    return await axios.post(`/users/${id}/reset-password`, { newPassword });
  },

  searchUsers: async ({ keyword, page = 0, size = 10, officeId, departmentId, positionId, active } = {}) => {
    return await axios.get("/users/search", {
      params: { keyword, page, size, officeId, departmentId, positionId, active },
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

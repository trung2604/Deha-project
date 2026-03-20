import axios from "@/utils/axios";

const departmentService = {
  getDepartments: async (params = {}) => {
    return await axios.get("/departments", { params });
  },

  getDepartment: async (id) => {
    return await axios.get(`/departments/${id}`);
  },

  createDepartment: async (departmentData) => {
    return await axios.post("/departments", departmentData);
  },

  updateDepartment: async (id, departmentData) => {
    return await axios.put(`/departments/${id}`, departmentData);
  },

  deleteDepartment: async (id) => {
    return await axios.delete(`/departments/${id}`);
  },
};

export default departmentService;

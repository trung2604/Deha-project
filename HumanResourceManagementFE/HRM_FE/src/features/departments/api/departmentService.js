import axios from "@/utils/axios";

const normalizeResponse = (response) => {
  if (!response || typeof response !== "object") return response;
  if (response.data !== undefined) return response.data;
  return response;
};

const departmentService = {
  getDepartments: async (params = {}) => {
    const res = await axios.get("/departments", { params });
    return normalizeResponse(res);
  },

  getDepartment: async (id) => {
    const res = await axios.get(`/departments/${id}`);
    return normalizeResponse(res);
  },

  createDepartment: async (departmentData) => {
    const res = await axios.post("/departments", departmentData);
    return normalizeResponse(res);
  },

  updateDepartment: async (id, departmentData) => {
    const res = await axios.put(`/departments/${id}`, departmentData);
    return normalizeResponse(res);
  },

  deleteDepartment: async (id) => {
    const res = await axios.delete(`/departments/${id}`);
    return normalizeResponse(res);
  },
};

export default departmentService;

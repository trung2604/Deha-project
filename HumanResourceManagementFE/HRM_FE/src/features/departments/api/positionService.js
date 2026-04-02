import axios from "@/utils/axios";

const positionService = {
  getPositions: async (params = {}) => {
    return await axios.get("/positions", { params });
  },

  getPosition: async (id) => {
    return await axios.get(`/positions/${id}`);
  },

  getDepartmentPositions: async (departmentId, params = {}) => {
    return await axios.get(`/departments/${departmentId}/positions`, { params });
  },

  createDepartmentPosition: async (departmentId, payload) => {
    return await axios.post(`/departments/${departmentId}/positions`, payload);
  },

  updateDepartmentPosition: async (departmentId, positionId, payload) => {
    return await axios.put(`/departments/${departmentId}/positions/${positionId}`, payload);
  },

  deleteDepartmentPosition: async (departmentId, positionId) => {
    return await axios.delete(`/departments/${departmentId}/positions/${positionId}`);
  },
};

export default positionService;


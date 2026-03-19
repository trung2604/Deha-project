import axios from "@/utils/axios";

const normalizeResponse = (response) => {
  if (!response || typeof response !== "object") return response;
  if (response.data !== undefined) return response.data;
  return response;
};

const positionService = {
  getPositions: async (params = {}) => {
    const res = await axios.get("/positions", { params });
    return normalizeResponse(res);
  },

  getPosition: async (id) => {
    const res = await axios.get(`/positions/${id}`);
    return normalizeResponse(res);
  },

  getDepartmentPositions: async (departmentId, params = {}) => {
    const res = await axios.get(`/departments/${departmentId}/positions`, { params });
    return normalizeResponse(res);
  },

  createDepartmentPosition: async (departmentId, payload) => {
    const res = await axios.post(`/departments/${departmentId}/positions`, payload);
    return normalizeResponse(res);
  },

  updateDepartmentPosition: async (departmentId, positionId, payload) => {
    const res = await axios.put(`/departments/${departmentId}/positions/${positionId}`, payload);
    return normalizeResponse(res);
  },

  deleteDepartmentPosition: async (departmentId, positionId) => {
    const res = await axios.delete(`/departments/${departmentId}/positions/${positionId}`);
    return normalizeResponse(res);
  },
};

export default positionService;


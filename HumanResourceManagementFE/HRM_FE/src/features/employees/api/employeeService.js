import axios from "@/utils/axios";

const normalizeResponse = (response) => {
  if (!response || typeof response !== "object") return response;
  if (response.data !== undefined) return response.data;
  return response;
};

const employeeService = {
  getEmployees: async (params = {}) => {
    const res = await axios.get("/employees", { params });
    return normalizeResponse(res);
  },

  getEmployee: async (id) => {
    const res = await axios.get(`/employees/${id}`);
    return normalizeResponse(res);
  },

  createEmployee: async (employeeData) => {
    const res = await axios.post("/employees", employeeData);
    return normalizeResponse(res);
  },

  updateEmployee: async (id, employeeData) => {
    const res = await axios.put(`/employees/${id}`, employeeData);
    return normalizeResponse(res);
  },

  deleteEmployee: async (id) => {
    const res = await axios.delete(`/employees/${id}`);
    return normalizeResponse(res);
  },

  searchEmployees: async (searchTerm) => {
    const res = await axios.get("/employees/search", {
      params: { q: searchTerm },
    });
    return normalizeResponse(res);
  },

  exportEmployees: async (format = "csv") => {
    return await axios.get("/employees/export", {
      params: { format },
      responseType: "blob",
    });
  },
};

export default employeeService;

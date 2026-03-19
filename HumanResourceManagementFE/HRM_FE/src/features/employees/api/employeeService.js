import axios from '@/utils/axios';

const employeeService = {
  getEmployees: async (params = {}) => {
    const response = await axios.get('/employees', { params });
    return response.data;
  },

  getEmployee: async (id) => {
    const response = await axios.get(`/employees/${id}`);
    return response.data;
  },

  createEmployee: async (employeeData) => {
    const response = await axios.post('/employees', employeeData);
    return response.data;
  },

  updateEmployee: async (id, employeeData) => {
    const response = await axios.put(`/employees/${id}`, employeeData);
    return response.data;
  },

  deleteEmployee: async (id) => {
    const response = await axios.delete(`/employees/${id}`);
    return response.data;
  },

  searchEmployees: async (searchTerm) => {
    const response = await axios.get('/employees/search', {
      params: { q: searchTerm },
    });
    return response.data;
  },

  exportEmployees: async (format = 'csv') => {
    const response = await axios.get('/employees/export', {
      params: { format },
      responseType: 'blob',
    });
    return response.data;
  },
};

export default employeeService;

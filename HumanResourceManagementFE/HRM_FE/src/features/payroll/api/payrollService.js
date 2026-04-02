import axios from "@/utils/axios";

const payrollService = {
  generatePayroll: async (payload) => {
    return await axios.post("/payrolls/generate", payload);
  },

  listPayrolls: async ({ year, month, officeId } = {}) => {
    return await axios.get("/payrolls", {
      params: { year, month, officeId },
    });
  },

  getPayrollDetailById: async (id) => {
    return await axios.get(`/payrolls/${id}`);
  },
};

export default payrollService;


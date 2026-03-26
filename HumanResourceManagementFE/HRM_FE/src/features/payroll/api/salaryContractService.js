import axios from "@/utils/axios";

const salaryContractService = {
  getByUser: async (userId) => {
    return await axios.get("/salary-contracts", { params: { userId } });
  },

  create: async (payload) => {
    return await axios.post("/salary-contracts", payload);
  },

  update: async (id, payload) => {
    return await axios.put(`/salary-contracts/${id}`, payload);
  },
};

export default salaryContractService;


import axios from "@/utils/axios";

const officeService = {
  getOffices: async () => {
    return await axios.get("/offices");
  },
  createOffice: async (payload) => {
    return await axios.post("/offices", payload);
  },
  updateOffice: async (id, payload) => {
    return await axios.put(`/offices/${id}`, payload);
  },
  deleteOffice: async (id) => {
    return await axios.delete(`/offices/${id}`);
  },
};

export default officeService;

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
  getMyOfficePolicy: async () => {
    return await axios.get("/offices/my-policy");
  },
  updateMyOfficePolicy: async (payload) => {
    return await axios.put("/offices/my-policy", payload);
  },
};

export default officeService;

import axios from "@/utils/axios";

const attendanceService = {
  checkIn: async () => await axios.post("/attendance/check-in"),
  checkOut: async () => await axios.post("/attendance/check-out"),
  getToday: async () => await axios.get("/attendance/today"),
  getDepartmentToday: async () => await axios.get("/attendance/department/today"),
  createOtRequest: async (payload) => await axios.post("/ot-requests", payload),
  getMyOtRequests: async () => await axios.get("/ot-requests/my"),
  getPendingOtRequests: async () => await axios.get("/ot-requests/pending"),
  decideOtRequest: async (id, payload) => await axios.patch(`/ot-requests/${id}/decision`, payload),
  createOtReport: async (payload) => await axios.post("/ot-reports", payload),
  getMyOtReports: async () => await axios.get("/ot-reports/my"),
  getPendingOtReports: async () => await axios.get("/ot-reports/pending"),
  decideOtReport: async (id, payload) => await axios.patch(`/ot-reports/${id}/decision`, payload),
};

export default attendanceService;

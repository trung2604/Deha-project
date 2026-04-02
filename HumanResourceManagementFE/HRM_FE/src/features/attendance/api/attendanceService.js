import axios from "@/utils/axios";
const attendanceService = {
  checkIn: async () => await axios.post("/attendance/check-in"),
  checkOut: async () => await axios.post("/attendance/check-out"),
  getToday: async () => await axios.get("/attendance/today"),
  getDepartmentToday: async () => await axios.get("/attendance/department/today"),
};

export default attendanceService;
import axios from "@/utils/axios";

const overtimeService = {
  // Overtime Requests
  getMyOvertimeRequests: async () => {
    return axios.get("/ot-requests/my");
  },

  getOvertimeRequestsByApprovalScope: async () => {
    return axios.get("/ot-requests");
  },

  createOvertimeRequest: async (data) => {
    return axios.post("/ot-requests", data);
  },

  decideOvertimeRequest: async (id, data) => {
    return axios.patch(`/ot-requests/${id}/decision`, data);
  },

  // Overtime Reports
  getMyOvertimeReports: async () => {
    return axios.get("/ot-reports/my");
  },

  getPendingOvertimeReports: async () => {
    return axios.get("/ot-reports/pending");
  },

  createOvertimeReport: async (data) => {
    return axios.post("/ot-reports", data);
  },

  decideOvertimeReport: async (id, data) => {
    return axios.patch(`/ot-reports/${id}/decision`, data);
  },

  // OT Sessions
  getTodayOvertimeSession: async () => {
    return axios.get("/ot-sessions/today");
  },

  checkInOvertimeSession: async () => {
    return axios.post("/ot-sessions/check-in");
  },

  checkOutOvertimeSession: async () => {
    return axios.post("/ot-sessions/check-out");
  },
};

export default overtimeService;



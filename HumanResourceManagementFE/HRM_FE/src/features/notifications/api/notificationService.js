import axios from "@/utils/axios";

const notificationService = {
  getMyNotifications: async ({ page = 0, size = 20 } = {}) => {
    return await axios.get("/notifications", { params: { page, size } });
  },

  markAsRead: async (id) => {
    return await axios.patch(`/notifications/${id}/read`);
  },

  markAllAsRead: async () => {
    return await axios.patch("/notifications/read-all");
  },
};

export default notificationService;


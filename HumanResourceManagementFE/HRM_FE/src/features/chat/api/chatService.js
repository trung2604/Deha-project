import axios from "@/utils/axios";

const chatService = {
  getMyRooms: async () => {
    return await axios.get("/chat/rooms");
  },

  getHistory: async (roomId, { page = 0, size = 30 } = {}) => {
    return await axios.get("/chat/history", {
      params: { roomId, page, size },
    });
  },
};

export default chatService;


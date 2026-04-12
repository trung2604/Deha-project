import axios from "@/utils/axios";

const auditLogService = {
  listAuditLogs: async ({
    page = 0,
    size = 20,
    actorUserId,
    httpMethod,
    endpointPattern,
    statusCode,
    success,
    targetId,
    from,
    to,
  } = {}) => {
    return await axios.get("/audit-logs", {
      params: {
        page,
        size,
        actorUserId,
        httpMethod,
        endpointPattern,
        statusCode,
        success,
        targetId,
        from,
        to,
      },
    });
  },

  getAuditLogById: async (id) => {
    return await axios.get(`/audit-logs/${id}`);
  },

  exportAuditLogsCsv: async ({
    actorUserId,
    httpMethod,
    endpointPattern,
    statusCode,
    success,
    targetId,
    from,
    to,
    limit = 5000,
  } = {}) => {
    return await axios.get("/audit-logs/export.csv", {
      params: {
        actorUserId,
        httpMethod,
        endpointPattern,
        statusCode,
        success,
        targetId,
        from,
        to,
        limit,
      },
      responseType: "blob",
      headers: {
        Accept: "text/csv",
      },
    });
  },
};

export default auditLogService;


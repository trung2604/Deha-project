export function isSuccessResponse(res) {
  return Boolean(res && typeof res.status === "number" && res.status >= 200 && res.status < 300);
}

export function getResponseStatus(res) {
  const status = res?.status ?? res?.statusCode;
  return typeof status === "number" ? status : null;
}

export function isConflictResponse(res) {
  return getResponseStatus(res) === 409;
}

export function isOptimisticConflictResponse(res) {
  if (!isConflictResponse(res)) return false;
  const message = String(res?.message ?? "").toLowerCase();
  return message.includes("modified by another user") || message.includes("refresh and retry");
}

export function getOptimisticConflictMessage(res, fallback = "Data is outdated. Please refresh and try again.") {
  return isOptimisticConflictResponse(res) ? getResponseMessage(res, fallback) : fallback;
}

export function getResponseMessage(res, fallback = "Request failed") {
  return res?.message || fallback;
}

export function getPageContent(res) {
  return Array.isArray(res?.data?.content) ? res.data.content : [];
}

export function getPageMeta(res) {
  return {
    page: typeof res?.data?.page === "number" ? res.data.page : 0,
    size: typeof res?.data?.size === "number" ? res.data.size : 10,
    totalPages: typeof res?.data?.totalPages === "number" ? res.data.totalPages : 0,
    totalElements: typeof res?.data?.totalElements === "number" ? res.data.totalElements : 0,
  };
}

export function getDepartmentDirectoryPayload(res) {
  const d = res?.data;
  if (Array.isArray(d)) {
    return { departments: d, totalCount: d.length };
  }
  return {
    departments: Array.isArray(d?.departments) ? d.departments : [],
    totalCount: typeof d?.totalCount === "number" ? d.totalCount : 0,
  };
}

export function getListData(res) {
  return Array.isArray(res?.data) ? res.data : [];
}

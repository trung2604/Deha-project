export function isSuccessResponse(res) {
  return Boolean(res && typeof res.status === "number" && res.status >= 200 && res.status < 300);
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

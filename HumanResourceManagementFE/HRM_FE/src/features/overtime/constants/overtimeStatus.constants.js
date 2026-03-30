export const OT_FILTER_ALL = "ALL";

export const OT_STATUS = {
  PENDING: "PENDING", // legacy value still returned in some records
  PENDING_DEPARTMENT: "PENDING_DEPARTMENT",
  PENDING_OFFICE: "PENDING_OFFICE",
  APPROVED: "APPROVED",
  REJECTED: "REJECTED",
  CANCELLED: "CANCELLED",
};

export const OT_STATUS_FILTER_OPTIONS = [
  { value: OT_FILTER_ALL, label: "All status" },
  { value: OT_STATUS.PENDING_DEPARTMENT, label: "Pending (Dept)" },
  { value: OT_STATUS.PENDING_OFFICE, label: "Pending (Office)" },
  { value: OT_STATUS.APPROVED, label: "Approved" },
  { value: OT_STATUS.REJECTED, label: "Rejected" },
];

export function isPendingStatus(status) {
  const normalized = String(status || "").toUpperCase();
  return normalized === OT_STATUS.PENDING || normalized.startsWith("PENDING");
}

export function getOtStatusMeta(status) {
  const normalized = String(status || "").toUpperCase();

  if (normalized === OT_STATUS.APPROVED) return { color: "success", label: "Approved" };
  if (normalized === OT_STATUS.REJECTED) return { color: "error", label: "Rejected" };
  if (normalized === OT_STATUS.CANCELLED) return { color: "default", label: "Cancelled" };
  if (normalized === OT_STATUS.PENDING_DEPARTMENT) return { color: "warning", label: "Pending (Dept)" };
  if (normalized === OT_STATUS.PENDING_OFFICE) return { color: "warning", label: "Pending (Office)" };

  return { color: "warning", label: "Pending" };
}


import { useState } from "react";
import { ChevronLeft, ChevronRight, Edit, KeyRound, Trash2, X } from "lucide-react";
import { Select, Spin } from "antd";
import { formatRoleLabel } from "@/utils/role";

const statusColors = {
  Active: { bg: "rgba(82, 196, 26, 0.1)", text: "#52C41A" },
  Inactive: { bg: "rgba(255, 77, 79, 0.1)", text: "#FF4D4F" },
};

export function UserTable({
  loading,
  resultAnimVersion = 0,
  users = [],
  onEdit,
  onDelete,
  onResetPassword,
  readOnly = false,
  totalPages,
  totalElements,
  page,
  size,
  onPageChange,
  onSizeChange,
}) {
  const [previewUser, setPreviewUser] = useState(null);

  function AvatarBadge({ user }) {
    const [imgFailed, setImgFailed] = useState(false);
    const initials = `${(user.firstName ?? "").charAt(0)}${(user.lastName ?? "").charAt(0)}`.toUpperCase() || "U";
    const canShowImage = Boolean(user.avatarUrl) && !imgFailed;

    return (
      <div
        className="w-9 h-9 rounded-full flex items-center justify-center text-white shrink-0 transition-transform duration-300 hover:scale-105 overflow-hidden"
        style={{
          background: "linear-gradient(135deg, #5B7CFF 0%, #7A5CFF 100%)",
          fontSize: "13px",
          fontWeight: "600",
          boxShadow: "0 6px 14px rgba(91,124,255,0.22)",
          cursor: canShowImage ? "zoom-in" : "default",
        }}
        onClick={() => {
          if (canShowImage) setPreviewUser(user);
        }}
        title={canShowImage ? "View avatar" : "No avatar"}
      >
        {canShowImage ? (
          <img
            src={user.avatarUrl}
            alt={fullName(user)}
            className="w-full h-full object-cover"
            onError={() => setImgFailed(true)}
          />
        ) : (
          initials
        )}
      </div>
    );
  }

  const list = Array.isArray(users) ? users : [];
  const showSkeletonOnly = loading && list.length === 0;
  const refetchingOverlay = loading && list.length > 0;

  const currentPage = Math.max(0, Number(page ?? 0));
  const pageCount = Math.max(0, Number(totalPages ?? 0));
  const currentSize = Math.max(1, Number(size ?? 10));
  const pageSizeOptions = [5, 10, 20, 50].includes(currentSize)
    ? [5, 10, 20, 50]
    : [currentSize, 5, 10, 20, 50];
  const canPrev = currentPage > 0;
  const canNext = pageCount > 0 ? currentPage + 1 < pageCount : false;

  const safeOnPageChange = (nextPage) => {
    if (typeof onPageChange !== "function") return;
    onPageChange(nextPage);
  };

  const safeOnSizeChange = (nextSize) => {
    if (typeof onSizeChange !== "function") return;
    onSizeChange(nextSize);
  };

  const formatCreatedAt = (value) => {
    if (!value) return "-";
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return "-";
    return d.toLocaleString(undefined, {
      month: "numeric",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const fullName = (user) =>
    `${user.firstName ?? ""} ${user.lastName ?? ""}`.trim() || "Unknown";

  return (
    <div
      className="rounded-xl overflow-hidden relative glass-surface page-surface"
      style={{
        boxShadow: "0 12px 28px rgba(35,57,110,0.11)",
      }}
    >
      {refetchingOverlay && (
        <div
          className="absolute inset-0 z-20 flex items-start justify-center pt-24 pointer-events-none"
          style={{
            background: "linear-gradient(180deg, rgba(255,255,255,0.72) 0%, rgba(255,255,255,0.5) 40%, transparent 100%)",
            backdropFilter: "blur(1px)",
          }}
        >
          <div
            className="pointer-events-none flex items-center gap-2 px-4 py-2 rounded-full border"
            style={{
              backgroundColor: "rgba(255,255,255,0.95)",
              borderColor: "rgba(22,119,255,0.2)",
              boxShadow: "0 8px 24px rgba(22,119,255,0.12)",
            }}
          >
            <Spin size="small" />
            <span style={{ color: "#595959", fontSize: "13px", fontWeight: 500 }}>Updating results…</span>
          </div>
        </div>
      )}

      <div
        className="md:hidden transition-opacity duration-300 ease-out"
        style={{ opacity: refetchingOverlay ? 0.55 : 1 }}
      >
        {showSkeletonOnly ? (
          <div className="p-4 space-y-3">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="h-24 rounded-xl shimmer" />
            ))}
          </div>
        ) : list.length === 0 ? (
          <div className="px-4 py-12 text-center">
            <p className="mb-1" style={{ color: "#0A0A0A", fontSize: "15px", fontWeight: 600 }}>
              No users found
            </p>
            <p style={{ color: "#8C8C8C", fontSize: "13px" }}>
              Try another keyword or change filters
            </p>
          </div>
        ) : (
          <div className="p-3 space-y-3">
            {list.map((user) => {
              const status = user.active ? "Active" : "Inactive";
              const colors = statusColors[status] ?? {
                bg: "rgba(0,0,0,0.04)",
                text: "#595959",
              };

              return (
                <div
                  key={`${resultAnimVersion}-${user.id}`}
                  className="rounded-xl border p-3 user-table-row-enter"
                  style={{ borderColor: "#E8E8E8", backgroundColor: "rgba(255,255,255,0.88)" }}
                >
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <div className="min-w-0">
                      <div className="mb-2">
                        <AvatarBadge user={user} />
                      </div>
                      <div style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: 600 }} className="truncate">
                        {fullName(user)}
                      </div>
                      <div style={{ color: "#595959", fontSize: "13px" }} className="truncate">
                        {user.email ?? "-"}
                      </div>
                    </div>
                    <span
                      className="px-2 py-0.5 rounded-full whitespace-nowrap"
                      style={{
                        backgroundColor: colors.bg,
                        color: colors.text,
                        fontSize: "12px",
                        fontWeight: "700",
                      }}
                    >
                      {status}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-x-3 gap-y-1 mb-3" style={{ fontSize: "12px" }}>
                    <span style={{ color: "#8C8C8C" }}>Role</span>
                    <span style={{ color: "#0A0A0A" }} className="truncate" title={user.role ?? "-"}>
                      {formatRoleLabel(user.role)}
                    </span>
                    <span style={{ color: "#8C8C8C" }}>Department</span>
                    <span style={{ color: "#0A0A0A" }} className="truncate">{user.departmentName ?? "-"}</span>
                    <span style={{ color: "#8C8C8C" }}>Position</span>
                    <span style={{ color: "#0A0A0A" }} className="truncate">{user.positionName ?? "-"}</span>
                    <span style={{ color: "#8C8C8C" }}>Created</span>
                    <span style={{ color: "#595959" }}>{formatCreatedAt(user.createdAt)}</span>
                  </div>

                  {!readOnly && (
                    <div className="flex items-center justify-end gap-2">
                      <button
                        type="button"
                        onClick={() => onEdit(user)}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-blue-50"
                        style={{ color: "#1677FF" }}
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      <button
                        type="button"
                        onClick={() => onDelete(user)}
                        disabled={!user.active}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-red-50"
                        style={{ color: user.active ? "#FF4D4F" : "#A0A0A0", cursor: user.active ? "pointer" : "not-allowed" }}
                        title={user.active ? "Delete user" : "User is already inactive"}
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                      <button
                        type="button"
                        onClick={() => onResetPassword(user)}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-amber-50"
                        style={{ color: "#FA8C16" }}
                        title="Reset password"
                      >
                        <KeyRound className="w-4 h-4" />
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div
        className="hidden md:block overflow-x-auto transition-opacity duration-300 ease-out"
        style={{ opacity: refetchingOverlay ? 0.55 : 1 }}
      >
        <table className="w-full min-w-[1040px] table-fixed">
          <thead style={{ background: "linear-gradient(135deg, #EEF3FF 0%, #F5F7FA 100%)" }}>
            <tr>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "18%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                User
              </th>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "18%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                Email
              </th>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "10%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                Role
              </th>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "10%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                Department
              </th>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "14%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                Position
              </th>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "8%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                Status
              </th>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "14%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                Created At
              </th>
              <th
                className="px-4 py-3 text-left uppercase tracking-wide"
                style={{ width: "8%", color: "#595959", fontSize: "11px", fontWeight: "600" }}
              >
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {showSkeletonOnly ? (
              Array.from({ length: 8 }).map((_, i) => (
                <tr key={i} className="border-t" style={{ borderColor: "#E8E8E8" }}>
                  <td className="px-4 py-4" colSpan={8}>
                    <div className="h-10 rounded shimmer" />
                  </td>
                </tr>
              ))
            ) : list.length === 0 ? (
              <tr>
                <td colSpan={8} className="px-4 py-16 text-center">
                  <p className="mb-1" style={{ color: "#0A0A0A", fontSize: "15px", fontWeight: 600 }}>
                    No users found
                  </p>
                  <p style={{ color: "#8C8C8C", fontSize: "13px" }}>
                    Try another keyword or change filters
                  </p>
                </td>
              </tr>
            ) : (
              list.map((user, index) => (
                <tr
                  key={`${resultAnimVersion}-${user.id}`}
                  className="border-t transition-colors duration-150 hover:bg-blue-50/30 user-table-row-enter"
                  style={{
                    borderColor: "#E8E8E8",
                    height: "56px",
                    animationDelay: `${Math.min(index, 14) * 38}ms`,
                  }}
                >
                  <td className="px-4 py-4">
                    <div className="flex items-center gap-3">
                      <AvatarBadge user={user} />
                      <div>
                        <div
                          style={{
                            color: "#0A0A0A",
                            fontSize: "14px",
                            fontWeight: "500",
                          }}
                        >
                          {fullName(user)}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-4 truncate">
                    <span className="block truncate" style={{ color: "#0A0A0A", fontSize: "14px" }}>
                      {user.email ?? "-"}
                    </span>
                  </td>
                  <td className="px-4 py-4 truncate">
                    <span
                      className="block truncate"
                      style={{ color: "#595959", fontSize: "14px" }}
                      title={user.role ?? "-"}
                    >
                      {formatRoleLabel(user.role)}
                    </span>
                  </td>
                  <td className="px-4 py-4 truncate">
                    <span className="block truncate" style={{ color: "#595959", fontSize: "14px" }}>
                      {user.departmentName ?? "-"}
                    </span>
                  </td>
                  <td className="px-4 py-4 truncate">
                    <span className="block truncate" style={{ color: "#595959", fontSize: "14px" }}>
                      {user.positionName ?? "-"}
                    </span>
                  </td>
                  <td className="px-4 py-4">
                    {(() => {
                      const status = user.active ? "Active" : "Inactive";
                      const colors = statusColors[status] ?? {
                        bg: "rgba(0,0,0,0.04)",
                        text: "#595959",
                      };
                      return (
                        <span
                          className="px-2 py-0.5 rounded-full"
                          style={{
                            backgroundColor: colors.bg,
                            color: colors.text,
                            fontSize: "12px",
                            fontWeight: "700",
                          }}
                        >
                          {status}
                        </span>
                      );
                    })()}
                  </td>
                  <td className="px-4 py-4 whitespace-nowrap">
                    <span
                      style={{
                        color: "#595959",
                        fontSize: "13px",
                        fontFamily: "JetBrains Mono, monospace",
                      }}
                    >
                      {formatCreatedAt(user.createdAt)}
                    </span>
                  </td>
                  <td className="px-4 py-4">
                    {readOnly ? (
                      <span style={{ color: "#A0A0A0", fontSize: "13px" }}>-</span>
                    ) : (
                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          onClick={() => onEdit(user)}
                          className="p-2 rounded-lg transition-colors duration-150 hover:bg-blue-50 hover-lift"
                          style={{ color: "#1677FF" }}
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => onDelete(user)}
                          disabled={!user.active}
                          className="p-2 rounded-lg transition-colors duration-150 hover:bg-red-50 hover-lift"
                          style={{ color: user.active ? "#FF4D4F" : "#A0A0A0", cursor: user.active ? "pointer" : "not-allowed" }}
                          title={user.active ? "Delete user" : "User is already inactive"}
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => onResetPassword(user)}
                          className="p-2 rounded-lg transition-colors duration-150 hover:bg-amber-50 hover-lift"
                          style={{ color: "#FA8C16" }}
                          title="Reset password"
                        >
                          <KeyRound className="w-4 h-4" />
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {!showSkeletonOnly && (
        <div
          className="px-6 py-4 border-t flex flex-col sm:flex-row sm:items-center justify-between gap-3"
          style={{ borderColor: "#E8E8E8" }}
        >
          <span style={{ color: "#595959", fontSize: "13px" }}>
            Showing {list.length} users{typeof totalElements === "number" ? ` (total: ${totalElements})` : ""}
          </span>

          <div className="flex items-center gap-2 flex-wrap justify-end">
            <Select
              value={currentSize}
              onChange={(value) => safeOnSizeChange(Number(value))}
              disabled={refetchingOverlay}
              style={{ width: 110 }}
              size="middle"
              options={pageSizeOptions.map((opt) => ({
                value: opt,
                label: `${opt}/page`,
              }))}
            />

            <button
              type="button"
              onClick={() => safeOnPageChange(Math.max(0, currentPage - 1))}
              disabled={!canPrev || refetchingOverlay}
              className="w-8 h-8 rounded-lg border flex items-center justify-center transition-colors duration-150"
              style={{
                borderColor: "#E8E8E8",
                color: canPrev ? "#1677FF" : "#A0A0A0",
                backgroundColor: canPrev ? "transparent" : "rgba(0,0,0,0.02)",
                cursor: canPrev ? "pointer" : "not-allowed",
              }}
            >
              <ChevronLeft className="w-4 h-4" />
            </button>

            <span style={{ color: "#595959", fontSize: "13px", minWidth: 92, textAlign: "center" }}>
              Page {pageCount > 0 ? currentPage + 1 : 0} / {pageCount}
            </span>

            <button
              type="button"
              onClick={() => safeOnPageChange(currentPage + 1)}
              disabled={!canNext || refetchingOverlay}
              className="w-8 h-8 rounded-lg border flex items-center justify-center transition-colors duration-150"
              style={{
                borderColor: "#E8E8E8",
                color: canNext ? "#1677FF" : "#A0A0A0",
                backgroundColor: canNext ? "transparent" : "rgba(0,0,0,0.02)",
                cursor: canNext ? "pointer" : "not-allowed",
              }}
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {previewUser?.avatarUrl && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          style={{ backgroundColor: "rgba(0,0,0,0.7)" }}
          onClick={() => setPreviewUser(null)}
        >
          <div
            className="w-full max-w-xl rounded-2xl overflow-hidden"
            style={{ backgroundColor: "#FFFFFF", border: "1px solid #E8E8E8" }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="px-4 py-3 flex items-center justify-between" style={{ borderBottom: "1px solid #E8E8E8" }}>
              <h3 style={{ color: "#0A0A0A", fontSize: "16px", fontWeight: 700, margin: 0 }}>
                {fullName(previewUser)}
              </h3>
              <button
                type="button"
                onClick={() => setPreviewUser(null)}
                className="p-2 rounded-lg"
                style={{ color: "#595959", backgroundColor: "#F5F5F5" }}
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="p-4 flex items-center justify-center" style={{ backgroundColor: "#0A0A0A" }}>
              <img src={previewUser.avatarUrl} alt={fullName(previewUser)} className="max-h-[70vh] w-auto rounded-lg object-contain" />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

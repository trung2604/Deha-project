import { ChevronLeft, ChevronRight, Edit, Trash2 } from "lucide-react";
import { Select } from "antd";

const statusColors = {
  Active: { bg: "rgba(82, 196, 26, 0.1)", text: "#52C41A" },
  Inactive: { bg: "rgba(255, 77, 79, 0.1)", text: "#FF4D4F" },
};

export function UserTable({ loading, users = [], onEdit, onDelete, totalPages, totalElements, page, size, onPageChange, onSizeChange }) {
  const list = Array.isArray(users) ? users : [];

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

  const formatRole = (role) => {
    if (!role) return "-";
    return String(role).replace(/^ROLE_/, "");
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

  return (
    <div
      className="rounded-xl overflow-hidden"
      style={{
        backgroundColor: "#FFFFFF",
        boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
      }}
    >
      <div className="overflow-x-auto">
        <table className="w-full table-fixed">
          <thead style={{ backgroundColor: "#F5F7FA" }}>
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
            {loading ? (
              Array.from({ length: 8 }).map((_, i) => (
                <tr
                  key={i}
                  className="border-t"
                  style={{ borderColor: "#E8E8E8" }}
                >
                  <td className="px-4 py-4" colSpan={8}>
                    <div className="h-10 rounded shimmer" />
                  </td>
                </tr>
              ))
            ) : list.length === 0 ? (
              <tr>
                <td colSpan={8} className="px-4 py-16 text-center">
                  <p style={{ color: "#595959", fontSize: "14px" }}>
                    No users found
                  </p>
                </td>
              </tr>
            ) : (
              list.map((user) => (
                <tr
                  key={user.id}
                  className="border-t transition-colors duration-150 hover:bg-blue-50/30"
                  style={{ borderColor: "#E8E8E8", height: "56px" }}
                >
                  <td className="px-4 py-4">
                    <div className="flex items-center gap-3">
                      <div
                        className="w-9 h-9 rounded-full flex items-center justify-center text-white shrink-0"
                        style={{
                          backgroundColor: "#1677FF",
                          fontSize: "13px",
                          fontWeight: "600",
                        }}
                      >
                        {`${(user.firstName ?? "").charAt(0)}${(user.lastName ?? "").charAt(0)}`.toUpperCase()}
                      </div>
                      <div>
                        <div
                          style={{
                            color: "#0A0A0A",
                            fontSize: "14px",
                            fontWeight: "500",
                          }}
                        >
                          {`${user.firstName ?? ""} ${user.lastName ?? ""}`.trim() ||
                            "Unknown"}
                        </div>
                        <div style={{ color: "#595959", fontSize: "13px" }}>
                          ID: {user.id ? String(user.id).slice(0, 8) : "-"}
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
                    <span className="block truncate" style={{ color: "#595959", fontSize: "14px" }} title={user.role ?? "-"}>
                      {formatRole(user.role)}
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
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => onEdit(user)}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-blue-50"
                        style={{ color: "#1677FF" }}
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => onDelete(user)}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-red-50"
                        style={{ color: "#FF4D4F" }}
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {!loading && (
        <div
          className="px-6 py-4 border-t flex items-center justify-between gap-4"
          style={{ borderColor: "#E8E8E8" }}
        >
          <span style={{ color: "#595959", fontSize: "13px" }}>
            Showing {list.length} users{typeof totalElements === "number" ? ` (total: ${totalElements})` : ""}
          </span>

          <div className="flex items-center gap-2">
            <Select
              value={currentSize}
              onChange={(value) => safeOnSizeChange(Number(value))}
              style={{ width: 110 }}
              size="middle"
              options={pageSizeOptions.map((opt) => ({
                value: opt,
                label: `${opt}/page`,
              }))}
            />

            <button
              onClick={() => safeOnPageChange(Math.max(0, currentPage - 1))}
              disabled={!canPrev}
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
              onClick={() => safeOnPageChange(currentPage + 1)}
              disabled={!canNext}
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
    </div>
  );
}

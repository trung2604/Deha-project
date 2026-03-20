import { Edit, Trash2 } from "lucide-react";

const statusColors = {
  Active: { bg: "rgba(82, 196, 26, 0.1)", text: "#52C41A" },
  Inactive: { bg: "rgba(255, 77, 79, 0.1)", text: "#FF4D4F" },
  "On Leave": { bg: "rgba(250, 140, 22, 0.1)", text: "#FA8C16" },
};

export function UserTable({ loading, users = [], onEdit, onDelete }) {
  const list = Array.isArray(users) ? users : [];
  return (
    <div
      className="rounded-xl overflow-hidden"
      style={{
        backgroundColor: "#FFFFFF",
        boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
      }}
    >
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead style={{ backgroundColor: "#F5F7FA" }}>
            <tr>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
              >
                User
              </th>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
              >
                Email
              </th>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
              >
                Role
              </th>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
              >
                Department
              </th>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
              >
                Position
              </th>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
              >
                Status
              </th>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
              >
                Created At
              </th>
              <th
                className="px-6 py-3 text-left uppercase tracking-wide"
                style={{
                  color: "#595959",
                  fontSize: "11px",
                  fontWeight: "600",
                }}
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
                  <td className="px-6 py-4" colSpan={8}>
                    <div className="h-10 rounded shimmer" />
                  </td>
                </tr>
              ))
            ) : list.length === 0 ? (
              <tr>
                <td colSpan={8} className="px-6 py-16 text-center">
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
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div
                        className="w-10 h-10 rounded-full flex items-center justify-center text-white shrink-0"
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
                          {user.email ?? "-"}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span style={{ color: "#0A0A0A", fontSize: "14px" }}>
                      {user.email ?? "-"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span style={{ color: "#595959", fontSize: "14px" }}>
                      {user.role ?? "-"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span style={{ color: "#595959", fontSize: "14px" }}>
                      {user.departmentName ?? "-"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span style={{ color: "#595959", fontSize: "14px" }}>
                      {user.positionName ?? "-"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
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
                  <td className="px-6 py-4">
                    <span
                      style={{
                        color: "#595959",
                        fontSize: "13px",
                        fontFamily: "JetBrains Mono, monospace",
                      }}
                    >
                      {user.createdAt
                        ? new Date(user.createdAt).toLocaleString()
                        : "-"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
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

      {!loading && list.length > 0 && (
        <div
          className="px-6 py-4 border-t flex items-center justify-end"
          style={{ borderColor: "#E8E8E8" }}
        >
          <span style={{ color: "#595959", fontSize: "13px" }}>
            Showing {list.length} of {list.length} users
          </span>
        </div>
      )}
    </div>
  );
}

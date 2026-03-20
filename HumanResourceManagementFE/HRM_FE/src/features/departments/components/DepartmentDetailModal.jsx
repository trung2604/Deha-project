import { useEffect, useMemo, useState } from "react";
import { Building2, Settings2, X } from "lucide-react";
import { toast } from "sonner";

import departmentService from "../api/departmentService";
import positionService from "../api/positionService";
import { PositionsModal } from "./PositionsModal";

export function DepartmentDetailModal({ open, departmentId, onClose, onPositionsChanged }) {
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [department, setDepartment] = useState(null);
  const [positionsModalOpen, setPositionsModalOpen] = useState(false);

  const positions = useMemo(() => department?.positions ?? [], [department]);
  const users = useMemo(() => department?.users ?? [], [department]);

  const refresh = async () => {
    if (!departmentId) return;
    setLoading(true);
    try {
      const res = await departmentService.getDepartment(departmentId);
      if (res?.status < 200 || res?.status >= 300) {
        toast.error(res?.message || "Failed to load department");
        setDepartment(null);
        return;
      }
      const next = res?.data ?? null;
      setDepartment(next);
      const posNames = (next?.positions ?? []).map((p) => p?.name).filter(Boolean);
      onPositionsChanged?.(departmentId, posNames);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!open) return;
    refresh().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, departmentId]);

  if (!open) return null;

  const handleCreatePosition = async (name) => {
    if (!departmentId) return;
    setSubmitting(true);
    try {
      const res = await positionService.createDepartmentPosition(departmentId, { name });
      if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to create position");
      toast.success(res?.message || "Position created successfully");
      await refresh();
    } catch {
      toast.error("Failed to create position");
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdatePosition = async (positionId, name) => {
    if (!departmentId) return;
    setSubmitting(true);
    try {
      const res = await positionService.updateDepartmentPosition(departmentId, positionId, { name });
      if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to update position");
      toast.success(res?.message || "Position updated successfully");
      await refresh();
    } catch {
      toast.error("Failed to update position");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeletePosition = async (positionId) => {
    if (!departmentId) return;
    setSubmitting(true);
    try {
      const res = await positionService.deleteDepartmentPosition(departmentId, positionId);
      if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to delete position");
      toast.success(res?.message || "Position deleted successfully");
      await refresh();
    } catch {
      toast.error("Failed to delete position");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div className="fixed inset-0 bg-black/50 z-40" onClick={onClose} />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[96vw] max-w-[980px] max-h-[86vh] rounded-2xl z-50 overflow-hidden"
        style={{ backgroundColor: "#FFFFFF", boxShadow: "0 20px 40px rgba(0,0,0,0.2)" }}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: "#E8E8E8" }}>
          <div className="flex items-center gap-3">
            <div
              className="w-10 h-10 rounded-full flex items-center justify-center"
              style={{ backgroundColor: "rgba(139, 92, 246, 0.1)" }}
            >
              <Building2 className="w-5 h-5" style={{ color: "#8B5CF6" }} />
            </div>
            <div>
              <div
                style={{
                  fontFamily: "DM Sans, sans-serif",
                  fontSize: "16px",
                  fontWeight: 700,
                  color: "#0A0A0A",
                  lineHeight: 1.2,
                }}
              >
                {department?.name ?? "Department detail"}
              </div>
              <div style={{ color: "#8C8C8C", fontSize: "13px", marginTop: "2px" }}>
                {loading ? "Loading…" : department?.id}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setPositionsModalOpen(true)}
              disabled={loading}
              className="flex items-center gap-2 px-3 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
              style={{
                backgroundColor: "rgba(139, 92, 246, 0.12)",
                color: "#8B5CF6",
                fontSize: "14px",
                fontWeight: "600",
              }}
            >
              <Settings2 className="w-4 h-4" />
              Manage positions
            </button>
            <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-lg transition-colors" title="Close">
              <X className="w-5 h-5" style={{ color: "#595959" }} />
            </button>
          </div>
        </div>

        <div className="p-6 overflow-y-auto" style={{ maxHeight: "calc(86vh - 66px)" }}>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-1 space-y-4">
              <div className="rounded-xl p-5 border" style={{ borderColor: "#E8E8E8" }}>
                <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, marginBottom: "6px" }}>
                  Description
                </div>
                <div style={{ color: "#0A0A0A", fontSize: "14px", lineHeight: 1.6 }}>
                  {department?.description || "—"}
                </div>
              </div>

              <div className="rounded-xl p-5 border" style={{ borderColor: "#E8E8E8" }}>
                <div className="flex items-center justify-between mb-3">
                  <div style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 700 }}>Positions</div>
                  <span
                    className="px-2 py-0.5 rounded-full"
                    style={{ backgroundColor: "rgba(22, 119, 255, 0.1)", color: "#1677FF", fontSize: "12px", fontWeight: 700 }}
                  >
                    {positions.length}
                  </span>
                </div>
                <div className="flex flex-wrap gap-2">
                  {positions.length === 0 ? (
                    <span style={{ color: "#8C8C8C", fontSize: "13px" }}>No positions</span>
                  ) : (
                    positions.map((p) => (
                      <span
                        key={p.id}
                        className="px-2.5 py-1 rounded-full"
                        style={{
                          backgroundColor: "rgba(22, 119, 255, 0.1)",
                          color: "#1677FF",
                          fontSize: "12px",
                          fontWeight: 700,
                        }}
                      >
                        {p.name}
                      </span>
                    ))
                  )}
                </div>
              </div>
            </div>

            <div className="lg:col-span-2">
              <div className="rounded-xl border overflow-hidden" style={{ borderColor: "#E8E8E8" }}>
                <div className="px-5 py-4 border-b" style={{ borderColor: "#E8E8E8" }}>
                  <div style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 800 }}>Users</div>
                  <div style={{ color: "#8C8C8C", fontSize: "13px", marginTop: "2px" }}>
                    {users.length} users
                  </div>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="text-left" style={{ backgroundColor: "#FAFAFA" }}>
                        <th className="px-5 py-3" style={{ color: "#595959", fontSize: "12px", fontWeight: 800 }}>
                          Name
                        </th>
                        <th className="px-5 py-3" style={{ color: "#595959", fontSize: "12px", fontWeight: 800 }}>
                          Email
                        </th>
                        <th className="px-5 py-3" style={{ color: "#595959", fontSize: "12px", fontWeight: 800 }}>
                          Position
                        </th>
                        <th className="px-5 py-3" style={{ color: "#595959", fontSize: "12px", fontWeight: 800 }}>
                          Role
                        </th>
                        <th className="px-5 py-3" style={{ color: "#595959", fontSize: "12px", fontWeight: 800 }}>
                          Status
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map((e) => (
                        <tr key={e.id} className="border-t" style={{ borderColor: "#F0F0F0" }}>
                          <td className="px-5 py-3" style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 600 }}>
                            {e.firstName} {e.lastName}
                          </td>
                          <td className="px-5 py-3" style={{ color: "#595959", fontSize: "13px" }}>
                            {e.email}
                          </td>
                          <td className="px-5 py-3" style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 600 }}>
                            {e.positionName || "—"}
                          </td>
                          <td className="px-5 py-3" style={{ color: "#595959", fontSize: "13px" }}>
                            {e.role}
                          </td>
                          <td className="px-5 py-3">
                            <span
                              className="px-2 py-0.5 rounded-full"
                              style={{
                                backgroundColor: e.active ? "rgba(82, 196, 26, 0.12)" : "rgba(250, 173, 20, 0.14)",
                                color: e.active ? "#52C41A" : "#FAAD14",
                                fontSize: "12px",
                                fontWeight: 800,
                              }}
                            >
                              {e.active ? "Active" : "Inactive"}
                            </span>
                          </td>
                        </tr>
                      ))}

                      {users.length === 0 && (
                        <tr>
                          <td colSpan={5} className="px-5 py-10" style={{ color: "#8C8C8C", fontSize: "14px" }}>
                            No users in this department.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <PositionsModal
          open={positionsModalOpen}
          department={department}
          positions={positions}
          onClose={() => setPositionsModalOpen(false)}
          onCreate={handleCreatePosition}
          onUpdate={handleUpdatePosition}
          onDelete={handleDeletePosition}
          submitting={submitting || loading}
        />
      </div>
    </>
  );
}


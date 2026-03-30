import { useEffect, useMemo, useState } from "react";
import { Building2, Settings2, X } from "lucide-react";
import { toast } from "sonner";
import { Spin } from "antd";
import {
  getOptimisticConflictMessage,
  getResponseMessage,
  isOptimisticConflictResponse,
  isSuccessResponse,
} from "@/utils/apiResponse";

import departmentService from "../api/departmentService";
import positionService from "../api/positionService";
import { PositionsModal } from "./PositionsModal";

export function DepartmentDetailModal({
  open,
  departmentId,
  onClose,
  onPositionsChanged,
  canManagePositions = false,
}) {
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [department, setDepartment] = useState(null);
  const [positionsModalOpen, setPositionsModalOpen] = useState(false);

  const [mounted, setMounted] = useState(open);
  const [closing, setClosing] = useState(false);

  const positions = useMemo(() => department?.positions ?? [], [department]);
  const users = useMemo(() => department?.users ?? [], [department]);

  const refresh = async () => {
    if (!departmentId) return;
    setLoading(true);
    try {
      const res = await departmentService.getDepartment(departmentId);
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load department"));
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

  useEffect(() => {
    if (open) {
      setMounted(true);
      setClosing(false);
      return;
    }
    if (!mounted) return;
    setClosing(true);
    const t = setTimeout(() => {
      setMounted(false);
      setClosing(false);
    }, 200);
    return () => clearTimeout(t);
  }, [open, mounted]);

  useEffect(() => {
    if (!open) {
      setPositionsModalOpen(false);
    }
  }, [open, departmentId]);

  if (!mounted) return null;

  const fadeStyle = {
    opacity: closing ? 0 : 1,
    transform: closing ? "translateY(10px) scale(0.99)" : "translateY(0px) scale(1)",
    transition: "opacity 180ms ease, transform 180ms ease",
  };

  const handleCreatePosition = async (name) => {
    if (!departmentId) return;
    setSubmitting(true);
    try {
      const res = await positionService.createDepartmentPosition(departmentId, { name });
      if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to create position"));
      toast.success(getResponseMessage(res, "Position created successfully"));
      await refresh();
    } catch {
      toast.error("Failed to create position");
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdatePosition = async (position, name) => {
    if (!departmentId) return;
    if (!position?.id) return;
    if (position?.version == null) {
      toast.error(getOptimisticConflictMessage());
      return;
    }
    setSubmitting(true);
    try {
      const res = await positionService.updateDepartmentPosition(departmentId, position.id, {
        name,
        expectedVersion: position.version,
      });
      if (!isSuccessResponse(res)) {
        return toast.error(
          isOptimisticConflictResponse(res)
            ? getOptimisticConflictMessage(res)
            : getResponseMessage(res, "Failed to update position"),
        );
      }
      toast.success(getResponseMessage(res, "Position updated successfully"));
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
      if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to delete position"));
      toast.success(getResponseMessage(res, "Position deleted successfully"));
      await refresh();
    } catch {
      toast.error("Failed to delete position");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div
        className="fixed inset-0 bg-black/50 z-40"
        onClick={onClose}
        style={{
          opacity: closing ? 0 : 1,
          transition: "opacity 180ms ease",
          pointerEvents: closing ? "none" : "auto",
        }}
      />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[980px] h-[720px] rounded-2xl z-50 overflow-hidden flex flex-col"
        style={{
          backgroundColor: "#FFFFFF",
          boxShadow: "0 20px 40px rgba(0,0,0,0.2)",
          maxWidth: "calc(100vw - 40px)",
          maxHeight: "calc(100vh - 40px)",
        }}
      >
        <div style={fadeStyle} className="relative h-full w-full flex flex-col min-h-0">
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
                  {loading
                    ? "Loading…"
                    : canManagePositions
                      ? "Positions and users in this department"
                      : "Department overview and team members"}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              {canManagePositions && (
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
              )}
              <button
                onClick={onClose}
                className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                title="Close"
              >
                <X className="w-5 h-5" style={{ color: "#595959" }} />
              </button>
            </div>
          </div>

          {loading && (
            <div
              className="absolute inset-0 z-10"
              style={{
                backgroundColor: "rgba(255,255,255,0.6)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Spin />
            </div>
          )}

          <div className="p-6 flex-1 overflow-hidden min-h-0">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-full min-h-0">
              <div className="lg:col-span-1 flex flex-col gap-4 min-h-0">
                <div className="rounded-xl p-5 border" style={{ borderColor: "#E8E8E8" }}>
                  <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, marginBottom: "6px" }}>
                    Description
                  </div>
                  <div style={{ color: "#0A0A0A", fontSize: "14px", lineHeight: 1.6 }}>
                    {department?.description || "—"}
                  </div>
                </div>

                <div
                  className="rounded-xl p-5 border flex flex-col flex-1 min-h-0"
                  style={{ borderColor: "#E8E8E8" }}
                >
                  <div className="flex items-center justify-between mb-3 flex-none">
                    <div style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 700 }}>Positions</div>
                    <span
                      className="px-2 py-0.5 rounded-full"
                      style={{
                        backgroundColor: "rgba(22, 119, 255, 0.1)",
                        color: "#1677FF",
                        fontSize: "12px",
                        fontWeight: 700,
                      }}
                    >
                      {positions.length}
                    </span>
                  </div>
                  <div className="flex-1 min-h-0 overflow-y-auto pr-1">
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
              </div>

              <div className="lg:col-span-2 flex flex-col min-h-0">
                <div
                  className="rounded-xl border overflow-hidden flex flex-col flex-1 min-h-0"
                  style={{ borderColor: "#E8E8E8" }}
                >
                  <div className="px-5 py-4 border-b" style={{ borderColor: "#E8E8E8" }}>
                    <div style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 800 }}>Users</div>
                    <div style={{ color: "#8C8C8C", fontSize: "13px", marginTop: "2px" }}>
                      {users.length} users
                    </div>
                  </div>

                  <div className="flex-1 min-h-0 overflow-y-auto overflow-x-auto">
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

          {canManagePositions && (
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
          )}
        </div>
      </div>
    </>
  );
}


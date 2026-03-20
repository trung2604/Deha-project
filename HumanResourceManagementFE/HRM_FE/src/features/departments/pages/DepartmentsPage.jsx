import { useEffect, useState } from "react";
import { Building2, Plus, Settings2, Trash2, Edit } from "lucide-react";
import { toast } from "sonner";
import { DepartmentDetailModal } from "../components/DepartmentDetailModal";

import departmentService from "../api/departmentService";
import positionService from "../api/positionService";
import { DepartmentModal } from "../components/DepartmentModal";
import { DeleteDepartmentModal } from "../components/DeleteDepartmentModal";
import { PositionsModal } from "../components/PositionsModal";

export function DepartmentsPage() {
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [departments, setDepartments] = useState([]);
  const [deptPositionsLoading, setDeptPositionsLoading] = useState(false);
  const [deptPositions, setDeptPositions] = useState([]);
  const [positionPreviewByDeptId, setPositionPreviewByDeptId] = useState({});
  const [positionPreviewLoadingByDeptId, setPositionPreviewLoadingByDeptId] = useState({});
  const [detailDepartmentId, setDetailDepartmentId] = useState(null);

  const [deptModalOpen, setDeptModalOpen] = useState(false);
  const [editingDepartment, setEditingDepartment] = useState(null);
  const [deletingDepartment, setDeletingDepartment] = useState(null);
  const [positionsDepartment, setPositionsDepartment] = useState(null);

  const departmentCount = departments.length;

  const refreshAll = async () => {
    setLoading(true);
    try {
      const deptRes = await departmentService.getDepartments();
      if (deptRes?.status < 200 || deptRes?.status >= 300) {
        toast.error(deptRes?.message || "Failed to load departments");
        setDepartments([]);
        return;
      }
      const list = Array.isArray(deptRes?.data) ? deptRes.data : deptRes?.data ?? [];
      setDepartments(Array.isArray(list) ? list : []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAll().catch(() => {});
  }, []);

  const loadPositionPreview = async (departmentId) => {
    if (!departmentId) return;
    setPositionPreviewLoadingByDeptId((p) => ({ ...p, [departmentId]: true }));
    try {
      const res = await positionService.getDepartmentPositions(departmentId);
      if (res?.status < 200 || res?.status >= 300) {
        toast.error(res?.message || "Failed to load positions");
        setPositionPreviewByDeptId((p) => ({ ...p, [departmentId]: [] }));
        return;
      }
      const list = Array.isArray(res?.data) ? res.data : res?.data ?? [];
      setPositionPreviewByDeptId((p) => ({
        ...p,
        [departmentId]: list.map((x) => x?.name).filter(Boolean),
      }));
    } finally {
      setPositionPreviewLoadingByDeptId((p) => ({ ...p, [departmentId]: false }));
    }
  };

  useEffect(() => {
    // best-effort preview load for visible list
    (async () => {
      for (const d of departments) {
        if (!d?.id) continue;
        if (positionPreviewByDeptId[d.id] !== undefined) continue;
        await loadPositionPreview(d.id);
      }
    })().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [departments]);

  const refreshDepartmentPositions = async (departmentId) => {
    if (!departmentId) return;
    setDeptPositionsLoading(true);
    try {
      const res = await positionService.getDepartmentPositions(departmentId);
      if (res?.status < 200 || res?.status >= 300) {
        toast.error(res?.message || "Failed to load positions");
        setDeptPositions([]);
        return;
      }
      const list = Array.isArray(res?.data) ? res.data : res?.data ?? [];
      setDeptPositions(Array.isArray(list) ? list : []);
    } finally {
      setDeptPositionsLoading(false);
    }
  };

  const openCreateDepartment = () => {
    setEditingDepartment(null);
    setDeptModalOpen(true);
  };

  const openEditDepartment = (dept) => {
    setEditingDepartment(dept);
    setDeptModalOpen(true);
  };

  const handleSubmitDepartment = async (payload) => {
    setSubmitting(true);
    try {
      if (editingDepartment?.id) {
        const res = await departmentService.updateDepartment(editingDepartment.id, payload);
        if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to save department");
        toast.success(res?.message || "Department updated successfully");
      } else {
        const res = await departmentService.createDepartment(payload);
        if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to save department");
        toast.success(res?.message || "Department created successfully");
      }
      setDeptModalOpen(false);
      setEditingDepartment(null);
      await refreshAll();
    } catch (e) {
      toast.error("Failed to save department");
      throw e;
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteDepartment = async () => {
    if (!deletingDepartment?.id) return;
    setSubmitting(true);
    try {
      const res = await departmentService.deleteDepartment(deletingDepartment.id);
      if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to delete department");
      toast.success(res?.message || "Department deleted successfully");
      setDeletingDepartment(null);
      await refreshAll();
    } catch (e) {
      toast.error("Failed to delete department");
      throw e;
    } finally {
      setSubmitting(false);
    }
  };

  const handleCreatePosition = async (positionPayload) => {
    const deptId = positionsDepartment?.id;
    if (!deptId) return;
    setSubmitting(true);
    try {
      const res = await positionService.createDepartmentPosition(deptId, { name: positionPayload });
      if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to create position");
      toast.success(res?.message || "Position created successfully");
      await refreshDepartmentPositions(deptId);
    } catch (e) {
      toast.error("Failed to create position");
      throw e;
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdatePosition = async (id, name) => {
    const deptId = positionsDepartment?.id;
    if (!deptId) return;
    setSubmitting(true);
    try {
      const res = await positionService.updateDepartmentPosition(deptId, id, { name });
      if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to update position");
      toast.success(res?.message || "Position updated successfully");
      await refreshDepartmentPositions(deptId);
    } catch (e) {
      toast.error("Failed to update position");
      throw e;
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeletePosition = async (id) => {
    const deptId = positionsDepartment?.id;
    if (!deptId) return;
    setSubmitting(true);
    try {
      const res = await positionService.deleteDepartmentPosition(deptId, id);
      if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to delete position");
      toast.success(res?.message || "Position deleted successfully");
      await refreshDepartmentPositions(deptId);
    } catch (e) {
      toast.error("Failed to delete position");
      throw e;
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1
            style={{
              fontFamily: "DM Sans, sans-serif",
              fontSize: "24px",
              fontWeight: "600",
              color: "#0A0A0A",
            }}
          >
            Departments
          </h1>
          <span
            className="px-3 py-1 rounded-full"
            style={{
              backgroundColor: "rgba(139, 92, 246, 0.1)",
              color: "#8B5CF6",
              fontSize: "13px",
              fontWeight: "600",
            }}
          >
            {departmentCount}
          </span>
        </div>

        <button
          onClick={openCreateDepartment}
          disabled={loading}
          className="flex items-center gap-2 px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
          style={{
            backgroundColor: "#1677FF",
            color: "#FFFFFF",
            fontSize: "14px",
            fontWeight: "500",
          }}
        >
          <Plus className="w-4 h-4" />
          Add Department
        </button>
      </div>

      {loading ? (
        <div className="rounded-xl p-6" style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
          <div style={{ color: "#8C8C8C", fontSize: "14px" }}>Loading departments…</div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {departments.map((dept) => {
            const preview = positionPreviewByDeptId[dept.id] ?? [];
            const previewLoading = !!positionPreviewLoadingByDeptId[dept.id];
            const previewTop = preview.slice(0, 4);
            return (
              <div
                key={dept.id}
                className="rounded-xl p-6 transition-all duration-200 hover:shadow-lg group relative cursor-pointer"
                style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                onClick={() => setDetailDepartmentId(dept.id)}
              >
                <div className="absolute top-4 right-4 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-150">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setPositionsDepartment(dept);
                      refreshDepartmentPositions(dept.id).catch(() => {});
                    }}
                    className="p-2 rounded-lg transition-colors duration-150 hover:bg-purple-50"
                    style={{ color: "#8B5CF6", backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                    title="Manage positions"
                  >
                    <Settings2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      openEditDepartment(dept);
                    }}
                    className="p-2 rounded-lg transition-colors duration-150 hover:bg-blue-50"
                    style={{ color: "#1677FF", backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                    title="Edit department"
                  >
                    <Edit className="w-4 h-4" />
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setDeletingDepartment(dept);
                    }}
                    className="p-2 rounded-lg transition-colors duration-150 hover:bg-red-50"
                    style={{ color: "#FF4D4F", backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                    title="Delete department"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div className="flex items-start justify-between mb-4">
                  <div
                    className="w-12 h-12 rounded-full flex items-center justify-center"
                    style={{ backgroundColor: "rgba(139, 92, 246, 0.1)" }}
                  >
                    <Building2 className="w-6 h-6" style={{ color: "#8B5CF6" }} />
                  </div>
                  <span
                    className="px-2 py-1 rounded-full"
                    style={{
                      backgroundColor: "rgba(22, 119, 255, 0.1)",
                      color: "#1677FF",
                      fontSize: "12px",
                      fontWeight: "500",
                    }}
                    title="Manage positions"
                  >
                    Positions
                  </span>
                </div>

                <h3
                  className="mb-1"
                  style={{
                    fontFamily: "DM Sans, sans-serif",
                    fontSize: "18px",
                    fontWeight: "600",
                    color: "#0A0A0A",
                  }}
                >
                  {dept.name}
                </h3>

                <p className="mb-4" style={{ color: "#595959", fontSize: "13px" }}>
                  {dept.description || "—"}
                </p>

                <div className="mb-4">
                  <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 600, marginBottom: "8px" }}>
                    Positions
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {previewLoading ? (
                      <span style={{ color: "#8C8C8C", fontSize: "13px" }}>Loading…</span>
                    ) : previewTop.length === 0 ? (
                      <span style={{ color: "#8C8C8C", fontSize: "13px" }}>No positions</span>
                    ) : (
                      previewTop.map((name) => (
                        <span
                          key={`${dept.id}-${name}`}
                          className="px-2.5 py-1 rounded-full"
                          style={{
                            backgroundColor: "rgba(22, 119, 255, 0.1)",
                            color: "#1677FF",
                            fontSize: "12px",
                            fontWeight: 600,
                          }}
                        >
                          {name}
                        </span>
                      ))
                    )}
                    {preview.length > previewTop.length && (
                      <span
                        className="px-2.5 py-1 rounded-full"
                        style={{
                          backgroundColor: "rgba(0,0,0,0.04)",
                          color: "#595959",
                          fontSize: "12px",
                          fontWeight: 600,
                        }}
                      >
                        +{preview.length - previewTop.length}
                      </span>
                    )}
                  </div>
                </div>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setDetailDepartmentId(dept.id);
                  }}
                  className="w-full h-9 rounded-lg transition-all duration-150 hover:opacity-90"
                  style={{
                    backgroundColor: "rgba(139, 92, 246, 0.1)",
                    color: "#8B5CF6",
                    fontSize: "14px",
                    fontWeight: "500",
                  }}
                >
                  View details
                </button>
              </div>
            );
          })}
        </div>
      )}

      <DepartmentModal
        open={deptModalOpen}
        department={editingDepartment}
        onClose={() => {
          setDeptModalOpen(false);
          setEditingDepartment(null);
        }}
        onSubmit={handleSubmitDepartment}
        submitting={submitting}
      />

      <DeleteDepartmentModal
        open={!!deletingDepartment}
        departmentName={deletingDepartment?.name}
        onClose={() => setDeletingDepartment(null)}
        onConfirm={handleDeleteDepartment}
        submitting={submitting}
      />

      <PositionsModal
        open={!!positionsDepartment}
        department={positionsDepartment}
        positions={deptPositions}
        onClose={() => setPositionsDepartment(null)}
        onCreate={handleCreatePosition}
        onUpdate={handleUpdatePosition}
        onDelete={handleDeletePosition}
        submitting={submitting || deptPositionsLoading}
      />

      <DepartmentDetailModal
        open={!!detailDepartmentId}
        departmentId={detailDepartmentId}
        onClose={() => setDetailDepartmentId(null)}
        onPositionsChanged={(deptId, posNames) => {
          if (!deptId) return;
          setPositionPreviewByDeptId((p) => ({ ...p, [deptId]: posNames ?? [] }));
        }}
      />
    </div>
  );
}


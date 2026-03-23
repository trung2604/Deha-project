import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Building2, Plus, Settings2, Trash2, Edit, Search } from "lucide-react";
import { toast } from "sonner";
import { Input, Spin } from "antd";
import { DepartmentDetailModal } from "../components/DepartmentDetailModal";
import { getDepartmentDirectoryPayload, getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";

import departmentService from "../api/departmentService";
import positionService from "../api/positionService";
import { DepartmentModal } from "../components/DepartmentModal";
import { DeleteDepartmentModal } from "../components/DeleteDepartmentModal";
import { PositionsModal } from "../components/PositionsModal";

function DepartmentGridSkeleton() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {Array.from({ length: 6 }).map((_, i) => (
        <div
          key={i}
          className="rounded-2xl p-6 border"
          style={{
            backgroundColor: "rgba(255,255,255,0.94)",
            borderColor: "rgba(15,23,42,0.06)",
            boxShadow: "0 12px 28px rgba(15,23,42,0.06)",
          }}
        >
          <div className="flex justify-between mb-4">
            <div className="w-12 h-12 rounded-full shimmer" />
            <div className="h-6 w-20 rounded-full shimmer" />
          </div>
          <div className="h-5 w-3/4 rounded shimmer mb-2" />
          <div className="h-4 w-full rounded shimmer mb-1" />
          <div className="h-4 w-2/3 rounded shimmer mb-4" />
          <div className="flex gap-2 flex-wrap">
            <div className="h-7 w-16 rounded-full shimmer" />
            <div className="h-7 w-20 rounded-full shimmer" />
          </div>
        </div>
      ))}
    </div>
  );
}

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
  const [departmentTotalCount, setDepartmentTotalCount] = useState(0);

  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("");
  const prevLoadingRef = useRef(false);
  const [resultAnimVersion, setResultAnimVersion] = useState(0);

  const isSearchPending = useMemo(
    () => searchTerm.trim() !== debouncedSearchTerm,
    [searchTerm, debouncedSearchTerm],
  );

  useEffect(() => {
    const t = setTimeout(() => setDebouncedSearchTerm(searchTerm.trim()), 500);
    return () => clearTimeout(t);
  }, [searchTerm]);

  useEffect(() => {
    if (prevLoadingRef.current && !loading) {
      setResultAnimVersion((v) => v + 1);
    }
    prevLoadingRef.current = loading;
  }, [loading]);

  const showSkeletonOnly = loading && departments.length === 0;
  const refetchingOverlay = loading && departments.length > 0;

  const refreshAll = useCallback(async () => {
    setLoading(true);
    try {
      const params = debouncedSearchTerm ? { keyword: debouncedSearchTerm } : {};
      const deptRes = await departmentService.getDepartments(params);
      if (!isSuccessResponse(deptRes)) {
        toast.error(getResponseMessage(deptRes, "Failed to load departments"));
        setDepartments([]);
        setDepartmentTotalCount(0);
        return;
      }
      const { departments: list, totalCount } = getDepartmentDirectoryPayload(deptRes);
      setDepartments(list);
      setDepartmentTotalCount(totalCount);
    } finally {
      setLoading(false);
    }
  }, [debouncedSearchTerm]);

  useEffect(() => {
    refreshAll().catch(() => {});
  }, [refreshAll]);

  const loadPositionPreview = async (departmentId) => {
    if (!departmentId) return;
    setPositionPreviewLoadingByDeptId((p) => ({ ...p, [departmentId]: true }));
    try {
      const res = await positionService.getDepartmentPositions(departmentId);
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load positions"));
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
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load positions"));
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
        if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to save department"));
        toast.success(getResponseMessage(res, "Department updated successfully"));
      } else {
        const res = await departmentService.createDepartment(payload);
        if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to save department"));
        toast.success(getResponseMessage(res, "Department created successfully"));
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
      if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to delete department"));
      toast.success(getResponseMessage(res, "Department deleted successfully"));
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
      if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to create position"));
      toast.success(getResponseMessage(res, "Position created successfully"));
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
      if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to update position"));
      toast.success(getResponseMessage(res, "Position updated successfully"));
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
      if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to delete position"));
      toast.success(getResponseMessage(res, "Position deleted successfully"));
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
              background:
                "linear-gradient(135deg, rgba(139, 92, 246, 0.16), rgba(139, 92, 246, 0.08))",
              color: "#8B5CF6",
              fontSize: "13px",
              fontWeight: "600",
              boxShadow: "inset 0 0 0 1px rgba(139,92,246,0.2)",
            }}
          >
            {debouncedSearchTerm ? `${departments.length} / ${departmentTotalCount}` : departmentTotalCount}
          </span>
        </div>

        <button
          onClick={openCreateDepartment}
          disabled={showSkeletonOnly}
          className="flex items-center gap-2 px-4 h-9 rounded-xl transition-all duration-200 hover:opacity-95 disabled:opacity-60"
          style={{
            background: "linear-gradient(135deg, #1677FF 0%, #0958D9 100%)",
            color: "#FFFFFF",
            fontSize: "14px",
            fontWeight: "500",
            boxShadow: "0 8px 20px rgba(22,119,255,0.26)",
          }}
        >
          <Plus className="w-4 h-4" />
          Add Department
        </button>
      </div>

      {!showSkeletonOnly && (
        <div
          className="rounded-xl p-4"
          style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
        >
          <div className={`max-w-md ${isSearchPending ? "search-input-pending" : ""}`}>
            <Input
              placeholder="Search by name or description…"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              allowClear
              prefix={<Search className="w-4 h-4" style={{ color: "#595959" }} />}
              suffix={isSearchPending ? <Spin size="small" style={{ color: "#8B5CF6" }} /> : null}
              size="middle"
            />
          </div>
        </div>
      )}

      {showSkeletonOnly ? (
        <DepartmentGridSkeleton />
      ) : (
        <div className="relative">
          {refetchingOverlay && (
            <div
              className="absolute inset-0 z-20 flex items-start justify-center pt-16 pointer-events-none rounded-2xl"
              style={{
                background: "linear-gradient(180deg, rgba(255,255,255,0.75) 0%, rgba(255,255,255,0.45) 45%, transparent 100%)",
                backdropFilter: "blur(1px)",
              }}
            >
              <div
                className="pointer-events-none flex items-center gap-2 px-4 py-2 rounded-full border"
                style={{
                  backgroundColor: "rgba(255,255,255,0.95)",
                  borderColor: "rgba(139,92,246,0.22)",
                  boxShadow: "0 8px 24px rgba(139,92,246,0.12)",
                }}
              >
                <Spin size="small" />
                <span style={{ color: "#595959", fontSize: "13px", fontWeight: 500 }}>Updating departments…</span>
              </div>
            </div>
          )}

          <div
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 transition-opacity duration-300 ease-out"
            style={{ opacity: refetchingOverlay ? 0.55 : 1 }}
          >
            {departments.length === 0 ? (
              <div
                className="col-span-full rounded-2xl p-12 text-center border"
                style={{
                  backgroundColor: "#FFFFFF",
                  borderColor: "#E8E8E8",
                  boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
                }}
              >
                <p className="mb-1" style={{ color: "#0A0A0A", fontSize: "15px", fontWeight: 600 }}>
                  {departmentTotalCount === 0 ? "No departments yet" : "No departments match your search"}
                </p>
                <p style={{ color: "#8C8C8C", fontSize: "13px" }}>
                  {departmentTotalCount === 0
                    ? "Create a department to get started"
                    : "Try another keyword"}
                </p>
              </div>
            ) : (
              departments.map((dept, index) => {
                const preview = positionPreviewByDeptId[dept.id] ?? [];
                const previewLoading = !!positionPreviewLoadingByDeptId[dept.id];
                const previewTop = preview.slice(0, 4);
                return (
                  <div
                    key={`${resultAnimVersion}-${dept.id}`}
                    className="rounded-2xl p-6 transition-all duration-200 hover:-translate-y-0.5 group relative cursor-pointer dept-card-enter"
                    style={{
                      backgroundColor: "rgba(255,255,255,0.94)",
                      boxShadow: "0 12px 28px rgba(15,23,42,0.08)",
                      border: "1px solid rgba(15,23,42,0.06)",
                      animationDelay: `${Math.min(index, 14) * 38}ms`,
                    }}
                    onClick={() => !refetchingOverlay && setDetailDepartmentId(dept.id)}
                  >
                    <div className="absolute top-4 right-4 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-150">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setPositionsDepartment(dept);
                          refreshDepartmentPositions(dept.id).catch(() => {});
                        }}
                        disabled={refetchingOverlay}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-purple-50 disabled:opacity-50"
                        style={{ color: "#8B5CF6", backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                        title="Manage positions"
                      >
                        <Settings2 className="w-4 h-4" />
                      </button>
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          openEditDepartment(dept);
                        }}
                        disabled={refetchingOverlay}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-blue-50 disabled:opacity-50"
                        style={{ color: "#1677FF", backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                        title="Edit department"
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setDeletingDepartment(dept);
                        }}
                        disabled={refetchingOverlay}
                        className="p-2 rounded-lg transition-colors duration-150 hover:bg-red-50 disabled:opacity-50"
                        style={{ color: "#FF4D4F", backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                        title="Delete department"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>

                    <div className="flex items-start justify-between mb-4">
                      <div
                        className="w-12 h-12 rounded-full flex items-center justify-center transition-transform duration-300 group-hover:scale-105"
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
                  </div>
                );
              })
            )}
          </div>
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

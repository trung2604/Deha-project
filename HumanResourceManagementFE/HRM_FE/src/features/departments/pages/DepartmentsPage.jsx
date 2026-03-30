import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Building2, Plus, Settings2, Trash2, Edit, Search, BriefcaseBusiness, Users, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { Input, Select, Spin } from "antd";
import { DepartmentDetailModal } from "../components/DepartmentDetailModal";
import {
  getDepartmentDirectoryPayload,
  getListData,
  getOptimisticConflictMessage,
  getResponseMessage,
  isOptimisticConflictResponse,
  isSuccessResponse,
} from "@/utils/apiResponse";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole, isDepartmentManagerRole, isOfficeManagerRole } from "@/utils/role";

import departmentService from "../api/departmentService";
import positionService from "../api/positionService";
import officeService from "@/features/offices/api/officeService";
import { DepartmentModal } from "../components/DepartmentModal";
import { DeleteDepartmentModal } from "../components/DeleteDepartmentModal";
import { PositionsModal } from "../components/PositionsModal";
import {
  DEPARTMENT_WORKSPACE_COPY,
  getDepartmentCountLabel,
  resolveManagerDepartment,
} from "../constants/departmentWorkspace.constants";

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
  const { user } = useAuth();
  const isAdmin = isAdminRole(user?.role);
  const officeManager = isOfficeManagerRole(user?.role);
  const departmentManager = isDepartmentManagerRole(user?.role);
  const canManageDepartments = isAdmin || officeManager; // Department manager is view-only
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [officesLoading, setOfficesLoading] = useState(true);
  const [offices, setOffices] = useState([]);
  const [officeFilter, setOfficeFilter] = useState(undefined);
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
    let cancelled = false;
    (async () => {
      setOfficesLoading(true);
      try {
        const res = isAdmin
          ? await officeService.getOffices()
          : { data: user?.officeId ? [{ id: user.officeId, name: user.officeName }] : [] };
        const list = getListData(res);
        if (!cancelled) {
          setOffices(list);
          if (!isAdmin && user?.officeId) {
            setOfficeFilter(user.officeId);
          }
        }
      } catch {
        if (!cancelled) toast.error("Failed to load offices");
      } finally {
        if (!cancelled) setOfficesLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [isAdmin, user?.officeId, user?.officeName]);

  useEffect(() => {
    if (prevLoadingRef.current && !loading) {
      setResultAnimVersion((v) => v + 1);
    }
    prevLoadingRef.current = loading;
  }, [loading]);

  const showSkeletonOnly = loading && departments.length === 0;
  const refetchingOverlay = loading && departments.length > 0;

  const myDepartment = useMemo(() => {
    if (!departmentManager) return null;
    return resolveManagerDepartment(departments, {
      departmentId: user?.departmentId,
      departmentName: user?.departmentName,
    });
  }, [departmentManager, departments, user?.departmentId, user?.departmentName]);

  const visibleDepartments = useMemo(() => {
    if (!departmentManager) return departments;
    return myDepartment ? [myDepartment] : [];
  }, [departmentManager, departments, myDepartment]);

  const managerPositionPreview = useMemo(() => {
    if (!myDepartment?.id) return [];
    return positionPreviewByDeptId[myDepartment.id] ?? [];
  }, [myDepartment?.id, positionPreviewByDeptId]);

  const refreshAll = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        ...(debouncedSearchTerm ? { keyword: debouncedSearchTerm } : {}),
        ...(officeFilter ? { officeId: officeFilter } : {}),
      };
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
  }, [debouncedSearchTerm, officeFilter]);

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
      for (const d of visibleDepartments) {
        if (!d?.id) continue;
        if (positionPreviewByDeptId[d.id] !== undefined) continue;
        await loadPositionPreview(d.id);
      }
    })().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visibleDepartments]);

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
      const finalOfficeId = payload?.officeId || officeFilter || user?.officeId;
      if (!finalOfficeId) {
        toast.error("Please select office");
        return;
      }
      const requestPayload = { ...payload, officeId: finalOfficeId };
      if (editingDepartment?.id) {
        if (requestPayload?.expectedVersion == null) {
          toast.error(getOptimisticConflictMessage());
          return;
        }
        const res = await departmentService.updateDepartment(editingDepartment.id, requestPayload);
        if (!isSuccessResponse(res)) {
          return toast.error(
            isOptimisticConflictResponse(res)
              ? getOptimisticConflictMessage(res)
              : getResponseMessage(res, "Failed to save department"),
          );
        }
        toast.success(getResponseMessage(res, "Department updated successfully"));
      } else {
        const res = await departmentService.createDepartment(requestPayload);
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

  const handleUpdatePosition = async (position, name) => {
    const deptId = positionsDepartment?.id;
    if (!deptId) return;
    if (!position?.id) return;
    if (position?.version == null) {
      toast.error(getOptimisticConflictMessage());
      return;
    }
    setSubmitting(true);
    try {
      const res = await positionService.updateDepartmentPosition(deptId, position.id, {
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
            {departmentManager ? DEPARTMENT_WORKSPACE_COPY.titleManager : DEPARTMENT_WORKSPACE_COPY.titleDefault}
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
            {getDepartmentCountLabel({
              departmentManager,
              myDepartment,
              visibleDepartments,
              debouncedSearchTerm,
              departmentTotalCount,
            })}
          </span>
        </div>

        {canManageDepartments && (
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
        )}
      </div>

      {departmentManager && (
        <div className="rounded-2xl p-5" style={{ background: "linear-gradient(135deg, rgba(22,119,255,0.10), rgba(139,92,246,0.10))", border: "1px solid rgba(22,119,255,0.2)" }}>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div style={{ color: "#0A0A0A", fontSize: "18px", fontWeight: 700 }}>
                {myDepartment?.name || user?.departmentName || "Department not assigned"}
              </div>
              <div style={{ color: "#595959", fontSize: "13px", marginTop: 4 }}>
                {DEPARTMENT_WORKSPACE_COPY.managerDescription}
              </div>
            </div>
            <div className="flex gap-2 flex-wrap">
              <span className="px-3 py-1 rounded-full" style={{ backgroundColor: "rgba(22,119,255,0.12)", color: "#1677FF", fontSize: 12, fontWeight: 700 }}>
                <BriefcaseBusiness className="w-3.5 h-3.5 inline mr-1" />
                {myDepartment?.officeName || user?.officeName || "Office"}
              </span>
              <span className="px-3 py-1 rounded-full" style={{ backgroundColor: "rgba(139,92,246,0.12)", color: "#8B5CF6", fontSize: 12, fontWeight: 700 }}>
                <Users className="w-3.5 h-3.5 inline mr-1" />
                {myDepartment?.id ? (positionPreviewByDeptId[myDepartment.id]?.length || 0) : 0} positions
              </span>
              <span className="px-3 py-1 rounded-full" style={{ backgroundColor: "rgba(82,196,26,0.12)", color: "#389E0D", fontSize: 12, fontWeight: 700 }}>
                <ShieldCheck className="w-3.5 h-3.5 inline mr-1" />
                {DEPARTMENT_WORKSPACE_COPY.managerViewBadge}
              </span>
            </div>
          </div>
        </div>
      )}

      {!showSkeletonOnly && !departmentManager && (
        <div
          className="rounded-xl p-4"
          style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
        >
          <div className="flex flex-wrap items-center gap-3">
            {isAdmin && (
              <Select
                value={officeFilter}
                onChange={(value) => setOfficeFilter(value ?? undefined)}
                placeholder="All Offices"
                allowClear
                loading={officesLoading}
                style={{ minWidth: 220 }}
                options={offices.map((o) => ({ value: o.id, label: o.name }))}
              />
            )}
            <div className={`flex-1 min-w-[220px] ${isSearchPending ? "search-input-pending" : ""}`}>
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
        </div>
      )}

      {showSkeletonOnly ? (
        <DepartmentGridSkeleton />
      ) : departmentManager ? (
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
                <span style={{ color: "#595959", fontSize: "13px", fontWeight: 500 }}>Updating department workspace…</span>
              </div>
            </div>
          )}

          {myDepartment ? (
            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 transition-opacity duration-300 ease-out" style={{ opacity: refetchingOverlay ? 0.55 : 1 }}>
              <div
                className="xl:col-span-2 rounded-2xl p-6 border"
                style={{
                  backgroundColor: "#FFFFFF",
                  borderColor: "rgba(15,23,42,0.08)",
                  boxShadow: "0 12px 28px rgba(15,23,42,0.08)",
                }}
              >
                <div className="flex items-start justify-between gap-4 mb-5">
                  <div className="flex items-center gap-4">
                    <div
                      className="w-14 h-14 rounded-full flex items-center justify-center"
                      style={{ backgroundColor: "rgba(139, 92, 246, 0.12)" }}
                    >
                      <Building2 className="w-7 h-7" style={{ color: "#8B5CF6" }} />
                    </div>
                    <div>
                      <h3 style={{ margin: 0, fontSize: "24px", fontWeight: 700, color: "#0A0A0A" }}>{myDepartment.name}</h3>
                      <p style={{ margin: "6px 0 0 0", color: "#595959", fontSize: "14px" }}>{myDepartment.description || "No description"}</p>
                    </div>
                  </div>
                  <span
                    className="px-3 py-1 rounded-full"
                    style={{
                      backgroundColor: "rgba(82,196,26,0.12)",
                      color: "#389E0D",
                      fontSize: "12px",
                      fontWeight: 700,
                    }}
                  >
                    Team Scope
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-5">
                  <div className="rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
                    <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, marginBottom: 6 }}>Office</div>
                    <div style={{ color: "#0A0A0A", fontSize: "15px", fontWeight: 700 }}>{myDepartment.officeName || user?.officeName || "-"}</div>
                  </div>
                  <div className="rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
                    <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, marginBottom: 6 }}>Visible Positions</div>
                    <div style={{ color: "#0A0A0A", fontSize: "15px", fontWeight: 700 }}>{managerPositionPreview.length}</div>
                  </div>
                  <div className="rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
                    <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, marginBottom: 6 }}>Access Mode</div>
                    <div style={{ color: "#0A0A0A", fontSize: "15px", fontWeight: 700 }}>{DEPARTMENT_WORKSPACE_COPY.managerViewBadge}</div>
                  </div>
                </div>

                <div>
                  <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, marginBottom: "8px", textTransform: "uppercase" }}>
                    Positions Snapshot
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {positionPreviewLoadingByDeptId[myDepartment.id] ? (
                      <span style={{ color: "#8C8C8C", fontSize: "13px" }}>Loading positions…</span>
                    ) : managerPositionPreview.length === 0 ? (
                      <span style={{ color: "#8C8C8C", fontSize: "13px" }}>No positions available</span>
                    ) : (
                      managerPositionPreview.slice(0, 8).map((name) => (
                        <span
                          key={`${myDepartment.id}-${name}`}
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
                    {managerPositionPreview.length > 8 && (
                      <span
                        className="px-2.5 py-1 rounded-full"
                        style={{
                          backgroundColor: "rgba(0,0,0,0.04)",
                          color: "#595959",
                          fontSize: "12px",
                          fontWeight: 600,
                        }}
                      >
                        +{managerPositionPreview.length - 8}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              <div
                className="rounded-2xl p-6 border"
                style={{
                  backgroundColor: "#FFFFFF",
                  borderColor: "rgba(15,23,42,0.08)",
                  boxShadow: "0 12px 28px rgba(15,23,42,0.08)",
                }}
              >
                <div style={{ color: "#0A0A0A", fontSize: "16px", fontWeight: 700, marginBottom: 6 }}>Department Actions</div>
                <p style={{ color: "#595959", fontSize: "13px", marginBottom: 16 }}>
                  Review full team members and position assignments in detail.
                </p>
                <button
                  type="button"
                  onClick={() => setDetailDepartmentId(myDepartment.id)}
                  className="w-full flex items-center justify-center gap-2 px-4 h-10 rounded-xl transition-all duration-200 hover:opacity-95"
                  style={{
                    background: "linear-gradient(135deg, #1677FF 0%, #0958D9 100%)",
                    color: "#FFFFFF",
                    fontSize: "14px",
                    fontWeight: "600",
                    boxShadow: "0 8px 20px rgba(22,119,255,0.26)",
                  }}
                >
                  Open Department Details
                </button>

                <div className="mt-5 rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
                  <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, textTransform: "uppercase", marginBottom: 8 }}>
                    Notes
                  </div>
                  <ul style={{ margin: 0, paddingLeft: 18, color: "#595959", fontSize: "13px", lineHeight: 1.7 }}>
                    <li>You have view access across departments but this page focuses your own team.</li>
                    <li>For position changes, contact office manager/admin.</li>
                    <li>Use Attendance and Overtime pages for daily operation workflows.</li>
                  </ul>
                </div>
              </div>
            </div>
          ) : (
            <div
              className="rounded-2xl p-12 text-center border"
              style={{
                backgroundColor: "#FFFFFF",
                borderColor: "#E8E8E8",
                boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
              }}
            >
              <p className="mb-1" style={{ color: "#0A0A0A", fontSize: "15px", fontWeight: 600 }}>
                {DEPARTMENT_WORKSPACE_COPY.managerNotAssignedTitle}
              </p>
              <p style={{ color: "#8C8C8C", fontSize: "13px" }}>
                {DEPARTMENT_WORKSPACE_COPY.managerNotAssignedDescription}
              </p>
            </div>
          )}
        </div>
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
            {visibleDepartments.length === 0 ? (
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
                  {departmentTotalCount === 0 ? "Create a department to get started" : "Try another keyword"}
                </p>
              </div>
            ) : (
              visibleDepartments.map((dept, index) => {
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
                    {canManageDepartments && (
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
                          style={{
                            color: "#8B5CF6",
                            backgroundColor: "#FFFFFF",
                            boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                          }}
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
                          style={{
                            color: "#1677FF",
                            backgroundColor: "#FFFFFF",
                            boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                          }}
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
                          style={{
                            color: "#FF4D4F",
                            backgroundColor: "#FFFFFF",
                            boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                          }}
                          title="Delete department"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    )}

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
        offices={offices}
        selectedOfficeId={officeFilter || user?.officeId || editingDepartment?.officeId}
        allowOfficeChange={isAdmin}
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
        canManagePositions={canManageDepartments}
        onPositionsChanged={(deptId, posNames) => {
          if (!deptId) return;
          setPositionPreviewByDeptId((p) => ({ ...p, [deptId]: posNames ?? [] }));
        }}
      />
    </div>
  );
}

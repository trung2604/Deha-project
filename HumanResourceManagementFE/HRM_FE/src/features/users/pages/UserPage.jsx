import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Plus } from "lucide-react";
import { Modal } from "antd";
import { toast } from "sonner";
import { UserFilters } from "../components/UserFilters";
import { UserTable } from "../components/UserTable";
import { UserModal } from "@/features/users/components/UserModal";
import { DeleteUserModal } from "@/features/users/components/DeleteUserModal";
import { ResetUserPasswordModal } from "@/features/users/components/ResetUserPasswordModal";
import UserService from "@/features/users/api/UserService";
import departmentService from "@/features/departments/api/departmentService";
import positionService from "@/features/departments/api/positionService";
import officeService from "@/features/offices/api/officeService";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole, isOfficeManagerRole } from "@/utils/role";
import {
  getOptimisticConflictMessage,
  isOptimisticConflictResponse,
  getDepartmentDirectoryPayload,
  getListData,
  getPageContent,
  getPageMeta,
  getResponseMessage,
  isConflictResponse,
  isSuccessResponse,
} from "@/utils/apiResponse";

export function UsersPage() {
  const normalizeEmail = (value) => String(value ?? "").trim().toLowerCase();
  const { user } = useAuth();
  const admin = isAdminRole(user?.role);
  const officeManager = isOfficeManagerRole(user?.role);
  const canEditUsers = admin || officeManager;
  const readOnly = !canEditUsers;
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("");
  const [officeFilter, setOfficeFilter] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState("");
  const [positionFilter, setPositionFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [deletingUser, setDeletingUser] = useState(null);
  const [resettingUser, setResettingUser] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [offices, setOffices] = useState([]);
  const [positions, setPositions] = useState([]);
  const prevLoadingRef = useRef(false);
  const [resultAnimVersion, setResultAnimVersion] = useState(0);

  const isSearchPending = useMemo(
    () => searchTerm.trim() !== debouncedSearchTerm,
    [searchTerm, debouncedSearchTerm],
  );

  useEffect(() => {
    if (prevLoadingRef.current && !loading) {
      setResultAnimVersion((v) => v + 1);
    }
    prevLoadingRef.current = loading;
  }, [loading]);

  useEffect(() => {
    let cancelled = false;
    async function loadFilters() {
      try {
        const officePromise = admin
          ? officeService.getOffices()
          : Promise.resolve({ data: user?.officeId ? [{ id: user.officeId, name: user.officeName }] : [] });
        const [officeRes, deptRes, posRes] = await Promise.all([
          officePromise,
          departmentService.getDepartments({ officeId: (admin ? officeFilter : user?.officeId) || undefined }),
          positionService.getPositions(),
        ]);
        if (!cancelled) {
          const officeList = getListData(officeRes);
          const { departments: deptList } = getDepartmentDirectoryPayload(deptRes);
          const posList = Array.isArray(posRes?.data) ? posRes.data : [];
          setOffices(officeList);
          setDepartments(deptList);
          setPositions(posList);
        }
      } catch {
        if (!cancelled) toast.error("Failed to load filter options");
      }
    }
    loadFilters();
    return () => {
      cancelled = true;
    };
  }, [admin, officeFilter, user?.officeId, user?.officeName]);

  useEffect(() => {
    let cancelled = false;

    const fetchUsers = async () => {
      setLoading(true);
      try {
        const officeId = (admin ? officeFilter : user?.officeId) || undefined;
        const keyword = (debouncedSearchTerm ?? "").trim() || undefined;
        const departmentId = departmentFilter || undefined;
        const positionId = positionFilter || undefined;
        const active = statusFilter === "" ? undefined : statusFilter === "true";
        const res = await UserService.getUsers({ keyword, officeId, departmentId, positionId, active, page, size });
        if (!isSuccessResponse(res)) throw new Error(getResponseMessage(res));
        const list = getPageContent(res);
        const meta = getPageMeta(res);
        if (!cancelled) {
          setUsers(Array.isArray(list) ? list : []);
          setTotalPages(meta.totalPages);
          setTotalElements(meta.totalElements);
          setPage(meta.page);
          setSize(meta.size);
        }
      } catch {
        if (!cancelled) toast.error("Failed to load Users");
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchUsers();
    return () => {
      cancelled = true;
    };
  }, [page, size, debouncedSearchTerm, admin, user?.officeId, officeFilter, departmentFilter, positionFilter, statusFilter]);

  const reloadUsers = useCallback(async () => {
    setLoading(true);
    try {
      const officeId = (admin ? officeFilter : user?.officeId) || undefined;
      const keyword = (debouncedSearchTerm ?? "").trim() || undefined;
      const departmentId = departmentFilter || undefined;
      const positionId = positionFilter || undefined;
      const active = statusFilter === "" ? undefined : statusFilter === "true";
      const res = await UserService.getUsers({ keyword, officeId, departmentId, positionId, active, page, size });
      if (!isSuccessResponse(res)) throw new Error(getResponseMessage(res));
      const list = getPageContent(res);
      const meta = getPageMeta(res);
      setUsers(Array.isArray(list) ? list : []);
      setTotalPages(meta.totalPages);
      setTotalElements(meta.totalElements);
      setPage(meta.page);
      setSize(meta.size);
    } catch {
      toast.error("Failed to load Users");
    } finally {
      setLoading(false);
    }
  }, [debouncedSearchTerm, admin, user?.officeId, officeFilter, departmentFilter, page, positionFilter, size, statusFilter]);

  // Debounce server-side search to avoid firing request on every keystroke.
  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm.trim());
      setPage(0); 
    }, 500);
    return () => clearTimeout(t);
  }, [searchTerm]);

  const filteredUsers = useMemo(() => users, [users]);
  const filteredPositions = useMemo(() => {
    return positions.filter((p) => {
      if (officeFilter && p.officeId !== officeFilter) return false;
      if (departmentFilter && p.departmentId !== departmentFilter) return false;
      return true;
    });
  }, [positions, officeFilter, departmentFilter]);

  const handleReset = () => {
    setSearchTerm("");
    setOfficeFilter(admin ? "" : (user?.officeId ?? ""));
    setDepartmentFilter("");
    setPositionFilter("");
    setStatusFilter("");
    setPage(0);
  };

  const handleDeleteConfirm = () => {
    if (!deletingUser) return;

    async function run() {
      try {
        const res = await UserService.deleteUser(deletingUser.id);
        if (!isSuccessResponse(res)) {
          if (isConflictResponse(res)) {
            Modal.confirm({
              title: "Cannot delete user",
              content: `${getResponseMessage(res, "This user has related records.")} Do you want to deactivate this user instead?`,
              okText: "Deactivate",
              cancelText: "Cancel",
              okButtonProps: { danger: true },
              onOk: async () => {
                const deactivateRes = await UserService.deactivateUser(deletingUser.id);
                if (!isSuccessResponse(deactivateRes)) {
                  toast.error(getResponseMessage(deactivateRes, "Failed to deactivate user"));
                  return;
                }
                await reloadUsers();
                toast.success(getResponseMessage(deactivateRes, "User deactivated successfully"));
              },
            });
            return;
          }
          return toast.error(getResponseMessage(res, "Failed to delete User"));
        }
        await reloadUsers();
        toast.success(getResponseMessage(res, "User deleted successfully"));
      } catch {
        toast.error("Failed to delete User");
      } finally {
        setDeletingUser(null);
      }
    }

    run();
  };

  const handleSave = async (userPayload) => {
    if (editingUser?.id && userPayload?.expectedVersion == null) {
      toast.error(getOptimisticConflictMessage());
      return;
    }
    setSaving(true);
    try {
      if (editingUser) {
        const res = await UserService.updateUser(userPayload.id, userPayload);
        if (!isSuccessResponse(res)) {
          return toast.error(
            isOptimisticConflictResponse(res)
              ? getOptimisticConflictMessage(res)
              : getResponseMessage(res, "Failed to save User"),
          );
        }
        await reloadUsers();
        toast.success(getResponseMessage(res, "User updated successfully"));
      } else {
        let res;
        try {
          res = await UserService.createUser(userPayload);
        } catch (error) {
          // Backend can still finish creating user when mail delivery is slow/fails.
          if (error?.code === "ECONNABORTED") {
            const officeId = (admin ? officeFilter : user?.officeId) || undefined;
            const verifyRes = await UserService.getUsers({
              keyword: userPayload?.email,
              officeId,
              page: 0,
              size: 20,
            });
            const created = getPageContent(verifyRes).some(
              (u) => normalizeEmail(u?.email) === normalizeEmail(userPayload?.email),
            );

            if (created) {
              await reloadUsers();
              toast.success("User was created. Verification email may be delayed due to SMTP connection issues.");
              setShowAddModal(false);
              setEditingUser(null);
              return;
            }

            toast.error("Create request timed out. Please retry or refresh list to confirm user status.");
            return;
          }
          throw error;
        }

        if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to save User"));
        await reloadUsers();
        toast.success(getResponseMessage(res, "User added successfully"));
      }

      setShowAddModal(false);
      setEditingUser(null);
    } catch {
      toast.error("Failed to save User");
    } finally {
      setSaving(false);
    }
  };

  const handleResetPasswordSubmit = async (newPassword) => {
    if (!resettingUser?.id) return;
    try {
      const res = await UserService.resetUserPassword(resettingUser.id, newPassword);
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to reset password"));
        return;
      }
      toast.success(getResponseMessage(res, "User password reset successfully"));
      setResettingUser(null);
    } catch {
      toast.error("Failed to reset password");
    }
  };

  const handleSearchTermChange = (value) => {
    setSearchTerm(value);
  };

  const handleOfficeFilterChange = (value) => {
    setOfficeFilter(value);
    setDepartmentFilter("");
    setPositionFilter("");
    setPage(0);
  };

  const handleDepartmentFilterChange = (value) => {
    setDepartmentFilter(value);
    setPositionFilter((currentPositionId) => {
      if (!value) return "";
      const isValid = positions.some(
        (p) => p.id === currentPositionId && p.departmentId === value,
      );
      return isValid ? currentPositionId : "";
    });
    setPage(0);
  };

  const handlePositionFilterChange = (value) => {
    setPositionFilter(value);
    setPage(0);
  };

  const handleStatusFilterChange = (value) => {
    setStatusFilter(value);
    setPage(0);
  };

  const handleSizeChange = (newSize) => {
    setSize(newSize);
    setPage(0); // Reset to first page when page size changes.
  };

  return (
    <div className="space-y-6">
      <div className="page-hero">
        <div className="flex items-center justify-between gap-4 flex-wrap">
          <div className="flex items-center gap-3">
            <h1 className="page-title">Users</h1>
            <span className="metric-chip">{totalElements}</span>
          </div>
          {canEditUsers && (
            <button
              onClick={() => {
                setEditingUser(null);
                setShowAddModal(true);
              }}
              className="btn-primary-gradient"
            >
              <Plus className="w-4 h-4" />
              Add User
            </button>
          )}
        </div>
        <p className="page-subtitle">
          Manage employee accounts, role assignments, and workspace placement.
        </p>
      </div>

      <UserFilters
        searchTerm={searchTerm}
        onSearchTermChange={handleSearchTermChange}
        isSearchPending={isSearchPending}
        officeFilter={admin ? officeFilter : (user?.officeId ?? "")}
        onOfficeFilterChange={handleOfficeFilterChange}
        departmentFilter={departmentFilter}
        onDepartmentFilterChange={handleDepartmentFilterChange}
        positionFilter={positionFilter}
        onPositionFilterChange={handlePositionFilterChange}
        statusFilter={statusFilter}
        onStatusFilterChange={handleStatusFilterChange}
        departments={departments}
        offices={admin ? offices : []}
        positions={filteredPositions}
        onReset={handleReset}
      />

      <UserTable
        loading={loading}
        resultAnimVersion={resultAnimVersion}
        users={filteredUsers}
        onEdit={setEditingUser}
        onDelete={setDeletingUser}
        onResetPassword={setResettingUser}
        readOnly={readOnly}
        totalPages={totalPages}
        totalElements={totalElements}
        page={page}
        size={size}
        onPageChange={setPage}
        onSizeChange={handleSizeChange}
      />

      {(showAddModal || editingUser) && (
        <UserModal
          user={editingUser}
          onClose={() => {
            setShowAddModal(false);
            setEditingUser(null);
          }}
          onSave={handleSave}
          submitting={saving}
        />
      )}

      {deletingUser && (
        <DeleteUserModal
          userName={
            `${deletingUser.firstName ?? ""} ${deletingUser.lastName ?? ""}`.trim() ||
            "-"
          }
          onClose={() => setDeletingUser(null)}
          onConfirm={handleDeleteConfirm}
        />
      )}

      {resettingUser && (
        <ResetUserPasswordModal
          user={resettingUser}
          onClose={() => setResettingUser(null)}
          onSubmit={handleResetPasswordSubmit}
        />
      )}
    </div>
  );
}

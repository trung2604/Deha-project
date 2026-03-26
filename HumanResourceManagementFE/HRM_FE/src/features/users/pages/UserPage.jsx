import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { UserFilters } from "../components/UserFilters";
import { UserTable } from "../components/UserTable";
import { UserModal } from "@/features/users/components/UserModal";
import { DeleteUserModal } from "@/features/users/components/DeleteUserModal";
import UserService from "@/features/users/api/UserService";
import departmentService from "@/features/departments/api/departmentService";
import positionService from "@/features/departments/api/positionService";
import officeService from "@/features/offices/api/officeService";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole, isDepartmentManagerRole, isOfficeManagerRole } from "@/utils/role";
import {
  getDepartmentDirectoryPayload,
  getListData,
  getPageContent,
  getPageMeta,
  getResponseMessage,
  isSuccessResponse,
} from "@/utils/apiResponse";

export function UsersPage() {
  const { user } = useAuth();
  const admin = isAdminRole(user?.role);
  const officeManager = isOfficeManagerRole(user?.role);
  const departmentManager = isDepartmentManagerRole(user?.role);
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
        if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to delete User"));
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
    setSaving(true);
    try {
      if (editingUser) {
        const res = await UserService.updateUser(userPayload.id, userPayload);
        if (!isSuccessResponse(res)) return toast.error(getResponseMessage(res, "Failed to save User"));
        await reloadUsers();
        toast.success(getResponseMessage(res, "User updated successfully"));
      } else {
        const res = await UserService.createUser(userPayload);
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
            Users
          </h1>
          <span
            className="px-3 py-1 rounded-full"
            style={{
              background:
                "linear-gradient(135deg, rgba(22, 119, 255, 0.14), rgba(22, 119, 255, 0.08))",
              color: "#1677FF",
              fontSize: "13px",
              fontWeight: "600",
              boxShadow: "inset 0 0 0 1px rgba(22,119,255,0.16)",
            }}
          >
            {totalElements}
          </span>
        </div>
        {canEditUsers && (
          <button
            onClick={() => {
              setEditingUser(null);
              setShowAddModal(true);
            }}
            className="flex items-center gap-2 px-4 h-9 rounded-xl transition-all duration-200 hover:opacity-95"
            style={{
              background:
                "linear-gradient(135deg, #1677FF 0%, #0958D9 100%)",
              color: "#FFFFFF",
              boxShadow: "0 8px 20px rgba(22,119,255,0.26)",
            }}
          >
            <Plus className="w-4 h-4" />
            <span style={{ fontSize: "14px", fontWeight: "500" }}>
              Add User
            </span>
          </button>
        )}
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
          UserName={
            `${deletingUser.firstName ?? ""} ${deletingUser.lastName ?? ""}`.trim() ||
            "-"
          }
          onClose={() => setDeletingUser(null)}
          onConfirm={handleDeleteConfirm}
        />
      )}
    </div>
  );
}

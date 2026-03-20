import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { UserFilters } from "../components/UserFilters";
import { UserTable } from "../components/UserTable";
import { UserModal } from "@/features/users/components/UserModal";
import { DeleteUserModal } from "@/features/users/components/DeleteUserModal";
import UserService from "@/features/users/api/UserService";

export function UsersPage() {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState("");
  const [positionFilter, setPositionFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [deletingUser, setDeletingUser] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      try {
        const keyword = (debouncedSearchTerm ?? "").trim();
        const res = keyword
          ? await UserService.searchUsers({ keyword, page, size })
          : await UserService.getUsers({ page, size });
        if (!res || typeof res.status !== "number") throw new Error("Invalid response");
        if (res.status < 200 || res.status >= 300) throw new Error(res.message || "Request failed");
        const list = Array.isArray(res.data.content) ? res.data.content : (res.data.content ?? []);
        setUsers(Array.isArray(list) ? list : []);
        setTotalPages(res.data.totalPages);
        setTotalElements(res.data.totalElements);
        setPage(res.data.page);
        setSize(res.data.size);
      } catch {
        toast.error("Failed to load Users");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [page, size, debouncedSearchTerm]);

  // Debounce server-side search to avoid firing request on every keystroke.
  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm.trim());
      setPage(0); // Reset to first page when keyword actually updates (debounced).
    }, 300);
    return () => clearTimeout(t);
  }, [searchTerm]);

  const reloadUsers = async () => {
    setLoading(true);
    try {
      const keyword = (debouncedSearchTerm ?? "").trim();
      const res = keyword
        ? await UserService.searchUsers({ keyword, page, size })
        : await UserService.getUsers({ page, size });
      if (!res || typeof res.status !== "number") throw new Error("Invalid response");
      if (res.status < 200 || res.status >= 300) throw new Error(res.message || "Request failed");
      const list = Array.isArray(res.data.content) ? res.data.content : (res.data.content ?? []);
      setUsers(Array.isArray(list) ? list : []);
      setTotalPages(res.data.totalPages);
      setTotalElements(res.data.totalElements);
      setPage(res.data.page);
      setSize(res.data.size);
    } catch {
      toast.error("Failed to load Users");
    } finally {
      setLoading(false);
    }
  };

  const departments = useMemo(
    () => [...new Set(users.map((e) => e.departmentName).filter(Boolean))],
    [users],
  );
  const positions = useMemo(
    () => [...new Set(users.map((e) => e.positionName).filter(Boolean))],
    [users],
  );

  // Server-side search is handled by the backend (keyword). Here we only apply
  // dropdown filters on the current server page.
  const filteredUsers = useMemo(() => {
    return users.filter((emp) => {
      const matchesDepartment =
        !departmentFilter || emp.departmentName === departmentFilter;
      const matchesPosition =
        !positionFilter || emp.positionName === positionFilter;
      const computedStatus = emp.active ? "Active" : "Inactive";
      const matchesStatus = !statusFilter || computedStatus === statusFilter;
      return matchesDepartment && matchesPosition && matchesStatus;
    });
  }, [users, departmentFilter, positionFilter, statusFilter]);

  const handleReset = () => {
    setSearchTerm("");
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
        if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to delete User");
        await reloadUsers();
        toast.success(res?.message || "User deleted successfully");
      } catch {
        toast.error("Failed to delete User");
      } finally {
        setDeletingUser(null);
      }
    }

    run();
  };

  const handleSave = async (User) => {
    setSaving(true);
    try {
      if (editingUser) {
        const res = await UserService.updateUser(User.id, User);
        if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to save User");
        await reloadUsers();
        toast.success(res?.message || "User updated successfully");
      } else {
        const res = await UserService.createUser(User);
        if (res?.status < 200 || res?.status >= 300) return toast.error(res?.message || "Failed to save User");
        await reloadUsers();
        toast.success(res?.message || "User added successfully");
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
              backgroundColor: "rgba(22, 119, 255, 0.1)",
              color: "#1677FF",
              fontSize: "13px",
              fontWeight: "600",
            }}
          >
            {totalElements}
          </span>
        </div>
        <button
          onClick={() => setShowAddModal(true)}
          className="flex items-center gap-2 px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90"
          style={{ backgroundColor: "#1677FF", color: "#FFFFFF" }}
        >
          <Plus className="w-4 h-4" />
          <span style={{ fontSize: "14px", fontWeight: "500" }}>
            Add User
          </span>
        </button>
      </div>

      <UserFilters
        searchTerm={searchTerm}
        onSearchTermChange={handleSearchTermChange}
        departmentFilter={departmentFilter}
        onDepartmentFilterChange={setDepartmentFilter}
        positionFilter={positionFilter}
        onPositionFilterChange={setPositionFilter}
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        departments={departments}
        positions={positions}
        onReset={handleReset}
      />

      <UserTable
        loading={loading}
        users={filteredUsers}
        onEdit={setEditingUser}
        onDelete={setDeletingUser}
        totalPages={totalPages}
        totalElements={totalElements}
        page={page}
        size={size}
        onPageChange={setPage}
        onSizeChange={handleSizeChange}
      />

      {(showAddModal || editingUser) && (
        <UserModal
          User={editingUser}
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

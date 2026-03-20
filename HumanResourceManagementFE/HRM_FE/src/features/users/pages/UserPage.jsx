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
  const [Users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
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
        const res = await UserService.getUsers();
        if (!res || typeof res.status !== "number") throw new Error("Invalid response");
        if (res.status < 200 || res.status >= 300) throw new Error(res.message || "Request failed");
        const list = Array.isArray(res.data) ? res.data : (res.data ?? []);
        if (!cancelled) setUsers(Array.isArray(list) ? list : []);
      } catch {
        if (!cancelled) toast.error("Failed to load Users");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const reloadUsers = async () => {
    setLoading(true);
    try {
      const res = await UserService.getUsers();
      if (!res || typeof res.status !== "number") throw new Error("Invalid response");
      if (res.status < 200 || res.status >= 300) throw new Error(res.message || "Request failed");
      const list = Array.isArray(res.data) ? res.data : (res.data ?? []);
      setUsers(Array.isArray(list) ? list : []);
    } catch {
      toast.error("Failed to load Users");
    } finally {
      setLoading(false);
    }
  };

  const departments = useMemo(
    () => [...new Set(Users.map((e) => e.departmentName).filter(Boolean))],
    [Users],
  );
  const positions = useMemo(
    () => [...new Set(Users.map((e) => e.positionName).filter(Boolean))],
    [Users],
  );

  const filteredUsers = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();
    return Users.filter((emp) => {
      const firstName = (emp.firstName ?? "").toString().toLowerCase();
      const lastName = (emp.lastName ?? "").toString().toLowerCase();
      const email = (emp.email ?? "").toString().toLowerCase();
      const matchesSearch =
        !query ||
        firstName.includes(query) ||
        lastName.includes(query) ||
        email.includes(query);
      const matchesDepartment =
        !departmentFilter || emp.departmentName === departmentFilter;
      const matchesPosition =
        !positionFilter || emp.positionName === positionFilter;
      const computedStatus = emp.active ? "Active" : "Inactive";
      const matchesStatus = !statusFilter || computedStatus === statusFilter;
      return (
        matchesSearch && matchesDepartment && matchesPosition && matchesStatus
      );
    });
  }, [Users, searchTerm, departmentFilter, positionFilter, statusFilter]);

  const handleReset = () => {
    setSearchTerm("");
    setDepartmentFilter("");
    setPositionFilter("");
    setStatusFilter("");
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
            {Users.length}
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
        onSearchTermChange={setSearchTerm}
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

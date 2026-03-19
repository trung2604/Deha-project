import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { EmployeeFilters } from "../components/EmployeeFilters";
import { EmployeeTable } from "../components/EmployeeTable";
import { EmployeeModal } from "../components/EmployeeModal";
import { DeleteEmployeeModal } from "../components/DeleteEmployeeModal";
import employeeService from "../api/employeeService";

export function EmployeesPage() {
  const [loading, setLoading] = useState(false);
  const [employees, setEmployees] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState("");
  const [positionFilter, setPositionFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [deletingEmployee, setDeletingEmployee] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      try {
        const data = await employeeService.getEmployees();
        const list = Array.isArray(data)
          ? data
          : (data?.items ?? data?.data ?? []);
        if (!cancelled) setEmployees(Array.isArray(list) ? list : []);
      } catch {
        if (!cancelled) toast.error("Failed to load employees");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const reloadEmployees = async () => {
    setLoading(true);
    try {
      const data = await employeeService.getEmployees();
      const list = Array.isArray(data) ? data : (data?.items ?? data?.data ?? []);
      setEmployees(Array.isArray(list) ? list : []);
    } catch {
      toast.error("Failed to load employees");
    } finally {
      setLoading(false);
    }
  };

  const departments = useMemo(
    () => [...new Set(employees.map((e) => e.departmentName).filter(Boolean))],
    [employees],
  );
  const positions = useMemo(
    () => [...new Set(employees.map((e) => e.positionName).filter(Boolean))],
    [employees],
  );

  const filteredEmployees = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();
    return employees.filter((emp) => {
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
      const matchesStatus = !statusFilter || emp.status === statusFilter;
      return (
        matchesSearch && matchesDepartment && matchesPosition && matchesStatus
      );
    });
  }, [employees, searchTerm, departmentFilter, positionFilter, statusFilter]);

  const handleReset = () => {
    setSearchTerm("");
    setDepartmentFilter("");
    setPositionFilter("");
    setStatusFilter("");
  };

  const handleDeleteConfirm = () => {
    if (!deletingEmployee) return;

    async function run() {
      try {
        await employeeService.deleteEmployee(deletingEmployee.id);
        await reloadEmployees();
        toast.success("Employee deleted successfully");
      } catch {
        toast.error("Failed to delete employee");
      } finally {
        setDeletingEmployee(null);
      }
    }

    run();
  };

  const handleSave = async (employee) => {
    try {
      if (editingEmployee) {
        await employeeService.updateEmployee(employee.id, employee);
        await reloadEmployees();
        toast.success("Employee updated successfully");
      } else {
        await employeeService.createEmployee(employee);
        await reloadEmployees();
        toast.success("Employee added successfully");
      }

      setShowAddModal(false);
      setEditingEmployee(null);
    } catch {
      toast.error("Failed to save employee");
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
            Employees
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
            {employees.length}
          </span>
        </div>
        <button
          onClick={() => setShowAddModal(true)}
          className="flex items-center gap-2 px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90"
          style={{ backgroundColor: "#1677FF", color: "#FFFFFF" }}
        >
          <Plus className="w-4 h-4" />
          <span style={{ fontSize: "14px", fontWeight: "500" }}>
            Add Employee
          </span>
        </button>
      </div>

      <EmployeeFilters
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

      <EmployeeTable
        loading={loading}
        employees={filteredEmployees}
        onEdit={setEditingEmployee}
        onDelete={setDeletingEmployee}
      />

      {(showAddModal || editingEmployee) && (
        <EmployeeModal
          employee={editingEmployee}
          onClose={() => {
            setShowAddModal(false);
            setEditingEmployee(null);
          }}
          onSave={handleSave}
        />
      )}

      {deletingEmployee && (
        <DeleteEmployeeModal
          employeeName={`${deletingEmployee.firstName ?? ""} ${deletingEmployee.lastName ?? ""}`.trim() || "-"}
          onClose={() => setDeletingEmployee(null)}
          onConfirm={handleDeleteConfirm}
        />
      )}
    </div>
  );
}

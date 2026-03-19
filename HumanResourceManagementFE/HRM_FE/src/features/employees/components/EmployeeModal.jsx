import { useEffect, useMemo, useState } from "react";
import { X } from "lucide-react";
import { toast } from "sonner";

import departmentService from "@/features/departments/api/departmentService";
import positionService from "@/features/departments/api/positionService";

export function EmployeeModal({ employee, onClose, onSave }) {
  const isEdit = !!employee?.id;

  const [departmentsLoading, setDepartmentsLoading] = useState(true);
  const [departments, setDepartments] = useState([]);
  const [positionsLoading, setPositionsLoading] = useState(false);
  const [positions, setPositions] = useState([]);

  // Backend expects { department: {id}, position: {id} }
  const [formData, setFormData] = useState(() => ({
    id: employee?.id,
    firstName: employee?.firstName ?? "",
    lastName: employee?.lastName ?? "",
    email: employee?.email ?? "",
    role: employee?.role ?? "",
    password: "",
    confirmPassword: "",
    departmentId: employee?.department?.id ?? "",
    positionId: employee?.position?.id ?? "",
  }));

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setDepartmentsLoading(true);
      try {
        const res = await departmentService.getDepartments();
        const list = Array.isArray(res) ? res : res ?? [];
        if (!cancelled) setDepartments(list);
      } catch {
        if (!cancelled) toast.error("Failed to load departments");
      } finally {
        if (!cancelled) setDepartmentsLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    const deptId = formData.departmentId;
    if (!deptId) {
      setPositions([]);
      setFormData((p) => ({ ...p, positionId: "" }));
      return;
    }

    (async () => {
      setPositionsLoading(true);
      try {
        const res = await positionService.getDepartmentPositions(deptId);
        const list = Array.isArray(res) ? res : res ?? [];
        if (!cancelled) setPositions(list);
      } catch {
        if (!cancelled) toast.error("Failed to load positions");
      } finally {
        if (!cancelled) setPositionsLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [formData.departmentId]);

  const selectedDepartmentName = useMemo(() => {
    const d = departments.find((x) => x?.id === formData.departmentId);
    return d?.name ?? "";
  }, [departments, formData.departmentId]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.departmentId) return toast.error("Please select a department");
    if (!formData.positionId) return toast.error("Please select a position");

    if (!isEdit) {
      if (!formData.password || formData.password.length < 8) {
        return toast.error("Password must be at least 8 characters");
      }
      if (formData.password !== formData.confirmPassword) {
        return toast.error("Password confirmation does not match");
      }
    }

    const payload = {
      ...(isEdit ? { id: formData.id } : {}),
      firstName: formData.firstName.trim(),
      lastName: formData.lastName.trim(),
      email: formData.email.trim(),
      role: formData.role,
      department: { id: formData.departmentId },
      position: { id: formData.positionId },
      ...(isEdit ? {} : { password: formData.password }),
    };

    onSave(payload);
  };

  return (
    <>
      <div className="fixed inset-0 bg-black/50 z-40" onClick={onClose} />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-[560px] rounded-xl z-50"
        style={{
          backgroundColor: "#FFFFFF",
          boxShadow: "0 20px 40px rgba(0,0,0,0.2)",
        }}
      >
        <div
          className="flex items-center justify-between px-6 py-4 border-b"
          style={{ borderColor: "#E8E8E8" }}
        >
          <h3
            style={{
              fontFamily: "DM Sans, sans-serif",
              fontSize: "16px",
              fontWeight: "600",
              color: "#0A0A0A",
            }}
          >
            {employee ? "Edit Employee" : "Add New Employee"}
          </h3>
          <button
            onClick={onClose}
            className="p-1 hover:bg-gray-100 rounded transition-colors"
          >
            <X className="w-5 h-5" style={{ color: "#595959" }} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6">
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div>
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> First Name
              </label>
              <input
                type="text"
                required
                value={formData.firstName}
                onChange={(e) =>
                  setFormData({ ...formData, firstName: e.target.value })
                }
                className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150"
                style={{ borderColor: "#E8E8E8" }}
              />
            </div>

            <div>
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Last Name
              </label>
              <input
                type="text"
                required
                value={formData.lastName}
                onChange={(e) =>
                  setFormData({ ...formData, lastName: e.target.value })
                }
                className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150"
                style={{ borderColor: "#E8E8E8" }}
              />
            </div>

            <div className="col-span-2">
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Email
              </label>
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) =>
                  setFormData({ ...formData, email: e.target.value })
                }
                className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150"
                style={{ borderColor: "#E8E8E8" }}
              />
            </div>

            <div className="col-span-2">
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Department
              </label>
              <select
                required
                value={formData.departmentId}
                onChange={(e) =>
                  setFormData((p) => ({
                    ...p,
                    departmentId: e.target.value,
                    positionId: "",
                  }))
                }
                disabled={departmentsLoading}
                className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150 disabled:opacity-60"
                style={{ borderColor: "#E8E8E8" }}
              >
                <option value="">
                  {departmentsLoading ? "Loading departments…" : "Select department"}
                </option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-span-2">
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Position
              </label>
              <select
                required
                value={formData.positionId}
                onChange={(e) =>
                  setFormData((p) => ({ ...p, positionId: e.target.value }))
                }
                disabled={!formData.departmentId || positionsLoading}
                className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150 disabled:opacity-60"
                style={{ borderColor: "#E8E8E8" }}
              >
                <option value="">
                  {!formData.departmentId
                    ? "Select department first"
                    : positionsLoading
                      ? `Loading positions for ${selectedDepartmentName || "department"}…`
                      : "Select position"}
                </option>
                {positions.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </div>

            {!isEdit && (
              <div>
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Password
              </label>
              <input
                type="password"
                required
                minLength={8}
                value={formData.password}
                onChange={(e) =>
                  setFormData({ ...formData, password: e.target.value })
                }
                className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150"
                style={{ borderColor: "#E8E8E8" }}
              />
              </div>
            )}

            {!isEdit && (
              <div>
                <label className="block mb-1.5">
                  <span style={{ color: "#FF4D4F" }}>*</span> Confirm Password
                </label>
                <input
                  type="password"
                  required
                  minLength={8}
                  value={formData.confirmPassword}
                  onChange={(e) =>
                    setFormData({ ...formData, confirmPassword: e.target.value })
                  }
                  className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150"
                  style={{ borderColor: "#E8E8E8" }}
                />
              </div>
            )}

            <div>
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Role
              </label>
              <select
                required
                value={formData.role}
                onChange={(e) =>
                  setFormData({ ...formData, role: e.target.value })
                }
                className="w-full h-9 px-3 rounded-lg border outline-none transition-all duration-150"
                style={{ borderColor: "#E8E8E8" }}
              >
                <option value="">Select role</option>
                <option value="HR">HR</option>
                <option value="ADMIN">ADMIN</option>
                <option value="EMPLOYEE">EMPLOYEE</option>
              </select>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 h-9 rounded-lg transition-colors duration-150 hover:bg-gray-100"
              style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: "500" }}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90"
              style={{
                backgroundColor: "#1677FF",
                color: "#FFFFFF",
                fontSize: "14px",
                fontWeight: "500",
              }}
            >
              {employee ? "Update" : "Submit"}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}

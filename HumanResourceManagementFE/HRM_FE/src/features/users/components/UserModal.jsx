import { useEffect, useMemo, useState } from "react";
import { X } from "lucide-react";
import { toast } from "sonner";
import { Input, Select } from "antd";
import departmentService from "@/features/departments/api/departmentService";
import positionService from "@/features/departments/api/positionService";
import officeService from "@/features/offices/api/officeService";
import { getDepartmentDirectoryPayload, getListData } from "@/utils/apiResponse";

export function UserModal({ user, onClose, onSave, submitting }) {
  const isEdit = !!user?.id;

  const [officesLoading, setOfficesLoading] = useState(true);
  const [offices, setOffices] = useState([]);
  const [departmentsLoading, setDepartmentsLoading] = useState(true);
  const [departments, setDepartments] = useState([]);
  const [positionsLoading, setPositionsLoading] = useState(false);
  const [positions, setPositions] = useState([]);

  const toFormData = (nextUser) => ({
    id: nextUser?.id,
    firstName: nextUser?.firstName ?? "",
    lastName: nextUser?.lastName ?? "",
    email: nextUser?.email ?? "",
    role: nextUser?.role ?? "",
    password: "",
    officeId: nextUser?.officeId ?? nextUser?.office?.id ?? undefined,
    departmentId: nextUser?.departmentId ?? nextUser?.department?.id ?? undefined,
    positionId: nextUser?.positionId ?? nextUser?.position?.id ?? undefined,
  });

  const [formData, setFormData] = useState(() => toFormData(user));
  const isOfficeManager = formData.role === "ROLE_MANAGER_OFFICE";
  const isDepartmentManager = formData.role === "ROLE_MANAGER_DEPARTMENT";
  const isManager = isOfficeManager || isDepartmentManager; // used for optional department/position UI in this component

  useEffect(() => {
    setFormData(toFormData(user));
  }, [user]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setOfficesLoading(true);
      try {
        const res = await officeService.getOffices();
        const list = getListData(res);
        if (!cancelled) setOffices(list);
      } catch {
        if (!cancelled) toast.error("Failed to load offices");
      } finally {
        if (!cancelled) setOfficesLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    const officeId = formData.officeId;
    if (!officeId) {
      setDepartments([]);
      setFormData((p) => ({ ...p, departmentId: undefined, positionId: undefined }));
      return;
    }
    (async () => {
      setDepartmentsLoading(true);
      try {
        const res = await departmentService.getDepartments({ officeId });
        const { departments: list } = getDepartmentDirectoryPayload(res);
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
  }, [formData.officeId]);

  useEffect(() => {
    let cancelled = false;
    const deptId = formData.departmentId;
    if (!deptId) {
      setPositions([]);
      setFormData((p) => ({ ...p, positionId: undefined }));
      return;
    }

    (async () => {
      setPositionsLoading(true);
      try {
        const res = await positionService.getDepartmentPositions(deptId);
        const list = Array.isArray(res?.data) ? res.data : (res?.data ?? []);
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.officeId) return toast.error("Please select an office");
    if (!formData.role) return toast.error("Please select a role");
    const hasDept = !!formData.departmentId;
    const hasPos = !!formData.positionId;

    if (isOfficeManager) {
      // For office manager: either pick both department+position, or leave both empty.
      if (hasDept !== hasPos) {
        return toast.error("For office manager, select both department and position (or leave both empty)");
      }
    } else if (isDepartmentManager) {
      // For department manager: department is required; position is optional.
      if (!hasDept) return toast.error("Please select a department for department manager");
    } else {
      // For employee (and legacy admin creation paths): require department + position.
      if (!hasDept) return toast.error("Please select a department");
      if (!hasPos) return toast.error("Please select a position");
    }

    if (!isEdit) {
      if (!formData.password || formData.password.length < 8) {
        return toast.error("Password must be at least 8 characters");
      }
    }

    const payload = {
      ...(isEdit ? { id: formData.id } : {}),
      firstName: formData.firstName.trim(),
      lastName: formData.lastName.trim(),
      email: formData.email.trim(),
      role: formData.role,
      office: { id: formData.officeId },
      ...(hasDept ? { department: { id: formData.departmentId } } : {}),
      ...(hasPos ? { position: { id: formData.positionId } } : {}),
      ...(isEdit ? {} : { password: formData.password }),
    };

    await onSave(payload);
  };

  return (
    <>
      <div
        className="fixed inset-0 bg-black/50 z-40"
        onClick={() => !submitting && onClose()}
      />
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
            {user ? "Edit User" : "Add New User"}
          </h3>
          <button
            onClick={onClose}
            disabled={submitting}
            className="p-1 hover:bg-gray-100 rounded transition-colors disabled:opacity-60"
          >
            <X className="w-5 h-5" style={{ color: "#595959" }} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6" autoComplete="off">
          <input type="text" name="fake-username" autoComplete="username" className="hidden" tabIndex={-1} />
          <input type="password" name="fake-password" autoComplete="new-password" className="hidden" tabIndex={-1} />
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div>
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> First Name
              </label>
              <Input
                required
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                disabled={submitting}
                size="middle"
                autoComplete="off"
              />
            </div>

            <div>
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Last Name
              </label>
              <Input
                required
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                disabled={submitting}
                size="middle"
                autoComplete="off"
              />
            </div>

            <div className="col-span-2">
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Email
              </label>
              <Input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                disabled={submitting}
                size="middle"
                autoComplete="new-email"
              />
            </div>

            <div className="col-span-2">
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Office
              </label>
              <Select
                value={formData.officeId}
                onChange={(value) =>
                  setFormData((p) => ({
                    ...p,
                    officeId: value ?? undefined,
                    departmentId: undefined,
                    positionId: undefined,
                  }))
                }
                disabled={officesLoading || submitting}
                placeholder={officesLoading ? "Loading offices…" : "Select office"}
                style={{ width: "100%" }}
                size="middle"
                options={offices.map((o) => ({ value: o.id, label: o.name }))}
              />
            </div>

            <div className="col-span-2">
              <label className="block mb-1.5">
                {isOfficeManager ? (
                  <span style={{ color: "#8C8C8C" }}>Department (optional)</span>
                ) : (
                  <>
                    <span placeholder="Select department" style={{ color: "#FF4D4F" }}>*</span> Department
                  </>
                )}
              </label>
              <Select
                value={formData.departmentId}
                onChange={(value) =>
                  setFormData((p) => ({
                    ...p,
                    departmentId: value ?? undefined,
                    positionId: undefined,
                  }))
                }
                disabled={!formData.officeId || departmentsLoading || submitting}
                placeholder={
                  !formData.officeId
                    ? "Select office first"
                    : departmentsLoading ? "Loading departments…" : "Select department"
                }
                style={{ width: "100%" }}
                size="middle"
                options={departments.map((d) => ({ value: d.id, label: d.name }))}
              />
            </div>

            <div className="col-span-2">
              <label className="block mb-1.5">
                {isManager ? (
                  <span style={{ color: "#8C8C8C" }}>Position (optional)</span>
                ) : (
                  <>
                    <span placeholder="Chosse department first" style={{ color: "#FF4D4F" }}>*</span> Position
                  </>
                )}
              </label>
              <Select
                value={formData.positionId}
                onChange={(value) =>
                  setFormData((p) => ({ ...p, positionId: value ?? undefined }))
                }
                disabled={
                  !formData.departmentId || positionsLoading || submitting
                }
                placeholder={
                  !formData.departmentId
                    ? "Select department first"
                    : positionsLoading
                      ? `Loading positions for ${selectedDepartmentName || "department"}…`
                      : "Select position"
                }
                style={{ width: "100%" }}
                size="middle"
                options={positions.map((p) => ({ value: p.id, label: p.name }))}
              />
            </div>

            {!isEdit && (
              <div>
                <label className="block mb-1.5">
                  <span style={{ color: "#FF4D4F" }}>*</span> Password
                </label>

                <Input.Password
                  placeholder="Enter password"
                  required
                  minLength={8}
                  value={formData.password}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                  disabled={submitting}
                  size="middle"
                  autoComplete="new-password"
                />
              </div>
            )}

            <div>
              <label className="block mb-1.5">
                <span style={{ color: "#FF4D4F" }}>*</span> Role
              </label>
              <Select
                value={formData.role}
                onChange={(value) => setFormData({ ...formData, role: value ?? "" })}
                disabled={submitting}
                placeholder="Select role"
                style={{ width: "100%" }}
                size="middle"
                options={[
                  { value: "ROLE_ADMIN", label: "ADMIN" },
                  { value: "ROLE_MANAGER_OFFICE", label: "OFFICE MANAGER" },
                  { value: "ROLE_MANAGER_DEPARTMENT", label: "DEPARTMENT MANAGER" },
                  { value: "ROLE_EMPLOYEE", label: "EMPLOYEE" },
                ]}
              />
            </div>
          </div>

          <div className="flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 h-9 rounded-lg transition-colors duration-150 hover:bg-gray-100 disabled:opacity-60"
              style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: "500" }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
              style={{
                backgroundColor: "#1677FF",
                color: "#FFFFFF",
                fontSize: "14px",
                fontWeight: "500",
              }}
            >
              {submitting ? "Saving..." : user ? "Update" : "Submit"}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}

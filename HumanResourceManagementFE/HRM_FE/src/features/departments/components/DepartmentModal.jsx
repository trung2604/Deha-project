import { useEffect, useMemo, useState } from "react";
import { X } from "lucide-react";
import { Input, Select } from "antd";

export function DepartmentModal({ open, department, offices = [], selectedOfficeId, allowOfficeChange = true, onClose, onSubmit, submitting }) {
  const initial = useMemo(
    () => ({
      version: department?.version,
      name: department?.name ?? "",
      description: department?.description ?? "",
      officeId: department?.officeId ?? selectedOfficeId ?? "",
    }),
    [department, selectedOfficeId]
  );

  const [form, setForm] = useState(initial);

  useEffect(() => {
    if (!open) return;
    // Defer to avoid React warning about setting state synchronously in effects.
    const t = setTimeout(() => {
      setForm(initial);
    }, 0);
    return () => clearTimeout(t);
  }, [open, initial]);

  if (!open) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.officeId) return;
    onSubmit?.({
      name: form.name,
      description: form.description,
      officeId: form.officeId,
      expectedVersion: form.version,
    });
  };

  return (
    <>
      <div className="fixed inset-0 bg-black/50 z-40" onClick={onClose} />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-[560px] rounded-xl z-50 glass-surface page-surface"
        style={{ boxShadow: "0 24px 46px rgba(35,57,110,0.24)" }}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: "#E8E8E8" }}>
          <h3
            style={{
              fontFamily: "DM Sans, sans-serif",
              fontSize: "16px",
              fontWeight: "600",
              color: "#0A0A0A",
            }}
          >
            {department ? "Edit Department" : "Add New Department"}
          </h3>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded transition-colors">
            <X className="w-5 h-5" style={{ color: "#595959" }} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5">
          <div className="space-y-3 mb-4">
            <div>
              <label className="block mb-1.5" style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: "500" }}>
                <span style={{ color: "#FF4D4F" }}>*</span> Office
              </label>
              <Select
                value={form.officeId}
                onChange={(value) => setForm((p) => ({ ...p, officeId: value ?? "" }))}
                disabled={!allowOfficeChange || submitting}
                options={offices.map((o) => ({ value: o.id, label: o.name }))}
                placeholder="Select office"
                style={{ width: "100%" }}
                size="middle"
              />
            </div>

            <div>
              <label className="block mb-1.5" style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: "500" }}>
                <span style={{ color: "#FF4D4F" }}>*</span> Department Name
              </label>
              <Input
                required
                value={form.name}
                onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
                placeholder="e.g. Engineering"
                style={{ width: "100%" }}
                size="middle"
              />
            </div>

            <div>
              <label className="block mb-1.5" style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: "500" }}>
                Description
              </label>
              <Input.TextArea
                value={form.description}
                onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                rows={4}
                style={{ fontSize: "14px", minHeight: "80px" }}
                placeholder="Brief description of the department"
                size="middle"
              />
            </div>
          </div>

          <div className="flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 h-9 rounded-lg transition-all duration-150 hover:bg-gray-100 disabled:opacity-60"
              disabled={submitting}
              style={{
                border: "1px solid #E8E8E8",
                color: "#595959",
                fontSize: "14px",
                fontWeight: "500",
              }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="btn-primary-gradient"
            >
              {department ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}


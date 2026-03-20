import { useEffect, useMemo, useState } from "react";
import { X } from "lucide-react";
import { Input } from "antd";

export function DepartmentModal({ open, department, onClose, onSubmit, submitting }) {
  const initial = useMemo(
    () => ({
      name: department?.name ?? "",
      description: department?.description ?? "",
    }),
    [department]
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
    onSubmit?.(form);
  };

  return (
    <>
      <div className="fixed inset-0 bg-black/50 z-40" onClick={onClose} />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-[560px] rounded-xl z-50"
        style={{ backgroundColor: "#FFFFFF", boxShadow: "0 20px 40px rgba(0,0,0,0.2)" }}
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
              className="px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
              style={{
                backgroundColor: "#1677FF",
                color: "#FFFFFF",
                fontSize: "14px",
                fontWeight: "500",
              }}
            >
              {department ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}


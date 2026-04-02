import { X } from "lucide-react";

export function DeleteDepartmentModal({ open, departmentName, onClose, onConfirm, submitting }) {
  if (!open) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black/50 z-40" onClick={onClose} />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-[440px] rounded-xl z-50"
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
            Delete Department
          </h3>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded transition-colors">
            <X className="w-5 h-5" style={{ color: "#595959" }} />
          </button>
        </div>

        <div className="p-6">
          <p style={{ color: "#595959", fontSize: "14px", lineHeight: "1.6" }}>
            Are you sure you want to delete{" "}
            <span style={{ fontWeight: "600", color: "#0A0A0A" }}>{departmentName}</span>? This action cannot be undone.
          </p>

          <div className="flex items-center justify-end gap-3 mt-6">
            <button
              onClick={onClose}
              disabled={submitting}
              className="px-4 h-9 rounded-lg transition-all duration-150 hover:bg-gray-100 disabled:opacity-60"
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
              onClick={onConfirm}
              disabled={submitting}
              className="px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
              style={{
                backgroundColor: "#FF4D4F",
                color: "#FFFFFF",
                fontSize: "14px",
                fontWeight: "500",
              }}
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </>
  );
}


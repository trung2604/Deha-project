import { useState } from "react";
import { X } from "lucide-react";

export function EmployeeModal({ employee, onClose, onSave }) {
  const [formData, setFormData] = useState(
    employee || {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      role: "",
    },
  );

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
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

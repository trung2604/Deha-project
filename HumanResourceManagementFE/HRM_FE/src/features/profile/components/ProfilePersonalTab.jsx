import { useEffect, useState } from "react";
import { Form, Input } from "antd";

function ReadOnlyField({ label, value }) {
  return (
    <div>
      <label className="block mb-2" style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: "500" }}>
        {label}
      </label>
      <Input
        value={value || "N/A"}
        readOnly
        style={{ borderColor: "#E8E8E8", backgroundColor: "#F5F7FA", color: "#595959" }}
        size="middle"
      />
    </div>
  );
}

export function ProfilePersonalTab({ user, onSave, saving }) {
  const [editing, setEditing] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    const timer = setTimeout(() => {
      form.setFieldsValue({
        firstName: user?.firstName ?? "",
        lastName: user?.lastName ?? "",
        phone: user?.phone ?? "",
      });
    }, 0);
    return () => clearTimeout(timer);
  }, [user, form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const ok = await onSave({
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        phone: (values.phone ?? "").trim(),
        expectedVersion: user?.version,
      });
      if (ok) setEditing(false);
    } catch {
      return;
    }
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h3 className="text-lg font-semibold" style={{ color: "#0A0A0A" }}>
          Personal Information
        </h3>
        {!editing ? (
          <button
            type="button"
            onClick={() => setEditing(true)}
            className="px-3 h-9 rounded-lg transition-all duration-150 hover:opacity-90"
            style={{ backgroundColor: "#1677FF", color: "#FFFFFF", fontSize: "14px", fontWeight: "500" }}
          >
            Edit Profile
          </button>
        ) : (
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => {
                setEditing(false);
                form.setFieldsValue({
                  firstName: user?.firstName ?? "",
                  lastName: user?.lastName ?? "",
                  phone: user?.phone ?? "",
                });
              }}
              className="px-3 h-9 rounded-lg transition-colors duration-150 hover:bg-gray-100"
              style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: "500" }}
              disabled={saving}
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleSave}
              className="px-3 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
              style={{ backgroundColor: "#1677FF", color: "#FFFFFF", fontSize: "14px", fontWeight: "500" }}
              disabled={saving}
            >
              {saving ? "Saving..." : "Save"}
            </button>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {editing ? (
          <Form
            form={form}
            layout="vertical"
            className="md:col-span-2"
            requiredMark={false}
          >
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Form.Item
                label="First Name"
                name="firstName"
                rules={[
                  { required: true, message: "First name is required" },
                  { whitespace: true, message: "First name cannot be blank" },
                ]}
                style={{ marginBottom: 0 }}
              >
                <Input size="middle" />
              </Form.Item>

              <Form.Item
                label="Last Name"
                name="lastName"
                rules={[
                  { required: true, message: "Last name is required" },
                  { whitespace: true, message: "Last name cannot be blank" },
                ]}
                style={{ marginBottom: 0 }}
              >
                <Input size="middle" />
              </Form.Item>

              <Form.Item
                label="Phone"
                name="phone"
                rules={[
                  {
                    validator: (_, value) => {
                      const normalized = (value ?? "").trim();
                      if (!normalized || /^\d{10}$/.test(normalized)) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error("Phone must be exactly 10 digits"));
                    },
                  },
                ]}
                style={{ marginBottom: 0 }}
              >
                <Input
                  maxLength={10}
                  inputMode="numeric"
                  onChange={(e) => {
                    const onlyDigits = e.target.value.replace(/\D/g, "");
                    form.setFieldValue("phone", onlyDigits);
                  }}
                  size="middle"
                />
              </Form.Item>
            </div>
            <p className="mt-1" style={{ color: "#8C8C8C", fontSize: "12px" }}>
              Enter 10 digits (numbers only)
            </p>
          </Form>
        ) : (
          <>
            <ReadOnlyField label="First Name" value={user?.firstName} />
            <ReadOnlyField label="Last Name" value={user?.lastName} />
          </>
        )}
        <ReadOnlyField label="Email" value={user?.email} />
        <ReadOnlyField label="Role" value={user?.role} />
        {!editing ? <ReadOnlyField label="Phone" value={user?.phone} /> : null}
      </div>

      <div className="mt-6 pt-6" style={{ borderTop: "1px solid #E8E8E8" }}>
        <h4 className="font-semibold mb-4" style={{ color: "#0A0A0A", fontSize: "14px" }}>
          Work Information
        </h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <ReadOnlyField label="Department" value={user?.departmentName} />
          <ReadOnlyField label="Position" value={user?.positionName} />
          <ReadOnlyField label="Status" value={user?.active ? "Active" : "Inactive"} />
        </div>
      </div>
    </div>
  );
}

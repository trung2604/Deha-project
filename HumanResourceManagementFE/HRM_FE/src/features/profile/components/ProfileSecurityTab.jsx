import { useState } from "react";
import { Alert, Button, Form, Input } from "antd";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

import { useAuth } from "@/features/auth/context/AuthContext";

export function ProfileSecurityTab() {
  const navigate = useNavigate();
  const { changePassword, logout } = useAuth();
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();

  const handleSubmit = async () => {
    const values = await form.validateFields();
    if (values.newPassword !== values.confirmPassword) {
      toast.error("Password confirmation does not match");
      return;
    }

    setSaving(true);
    try {
      const res = await changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });

      if (!res.ok) {
        toast.error(res.message || "Change password failed");
        return;
      }

      toast.success(res.message || "Password changed successfully. Please log in again.");
      form.resetFields();
      logout();
      navigate("/login", { replace: true });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <h3 className="text-lg font-semibold mb-4" style={{ color: "#0A0A0A" }}>
        Security
      </h3>
      <p className="mb-4" style={{ color: "#595959", fontSize: "14px" }}>
        Change your account password. Use at least 8 characters.
      </p>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        description="After changing password, you will be signed out and asked to log in again."
      />

      <Form form={form} layout="vertical" disabled={saving}>
        <Form.Item
          label="Current password"
          name="currentPassword"
          rules={[{ required: true, message: "Current password is required" }]}
        >
          <Input.Password placeholder="Enter current password" />
        </Form.Item>

        <Form.Item
          label="New password"
          name="newPassword"
          rules={[
            { required: true, message: "New password is required" },
            { min: 8, message: "Password must be at least 8 characters" },
          ]}
        >
          <Input.Password placeholder="Enter new password" />
        </Form.Item>

        <Form.Item
          label="Confirm new password"
          name="confirmPassword"
          rules={[{ required: true, message: "Password confirmation is required" }]}
        >
          <Input.Password placeholder="Re-enter new password" />
        </Form.Item>

        <Button
          type="primary"
          loading={saving}
          onClick={handleSubmit}
          style={{ borderRadius: 10, fontWeight: 600 }}
        >
          Change password
        </Button>
      </Form>
    </div>
  );
}

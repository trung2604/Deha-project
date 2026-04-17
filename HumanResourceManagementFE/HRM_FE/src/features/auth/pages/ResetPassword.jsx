import { useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Form, Input } from "antd";
import { toast } from "sonner";
import authService from "../api/authService";
import { getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";
import { AUTH_ACTION_PRIMARY_STYLE, AUTH_ACTION_SECONDARY_STYLE, AUTH_BG_STYLE, AUTH_CARD_STYLE } from "../constants/authUi";

export function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const resetToken = useMemo(() => searchParams.get("resetToken") || "", [searchParams]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const handleResetPassword = async () => {
    const values = await form.validateFields();
    if (!resetToken) {
      toast.error("Missing reset token");
      return;
    }
    if (values.newPassword !== values.confirmPassword) {
      toast.error("Password confirmation does not match");
      return;
    }

    setLoading(true);
    const res = await authService.resetPassword({
      resetToken,
      newPassword: values.newPassword,
    });
    setLoading(false);

    if (!isSuccessResponse(res)) {
      toast.error(getResponseMessage(res, "Failed to reset password"));
      return;
    }

    toast.success(getResponseMessage(res, "Password reset successfully"));
    navigate("/login", { replace: true });
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6" style={AUTH_BG_STYLE}>
      <div className="w-full max-w-lg rounded-2xl p-8 glass-surface page-surface" style={AUTH_CARD_STYLE}>
        <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 6, color: "#0A0A0A" }}>Reset password</h2>
        <p style={{ color: "#595959", marginBottom: 20 }}>Enter a new password for your account.</p>

        {!resetToken && (
          <Alert
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
            description="Reset link is invalid or expired"
          />
        )}

        <Form form={form} layout="vertical" disabled={loading || !resetToken}>
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
            label="Confirm password"
            name="confirmPassword"
            rules={[{ required: true, message: "Password confirmation is required" }]}
          >
            <Input.Password placeholder="Re-enter new password" />
          </Form.Item>

          <div className="flex gap-2">
            <Button style={AUTH_ACTION_SECONDARY_STYLE} onClick={() => navigate("/forgot-password")}>Back</Button>
            <Button type="primary" style={AUTH_ACTION_PRIMARY_STYLE} loading={loading} onClick={handleResetPassword} disabled={!resetToken}>
              Reset password
            </Button>
          </div>
        </Form>
      </div>
    </div>
  );
}


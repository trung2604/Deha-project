import { useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Form, Input } from "antd";
import { toast } from "sonner";
import authService from "../api/authService";
import { getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";
import { AUTH_ACTION_PRIMARY_STYLE, AUTH_ACTION_SECONDARY_STYLE, AUTH_BG_STYLE, AUTH_CARD_STYLE } from "../constants/authUi";

export function ForgotPassword() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialEmail = useMemo(() => searchParams.get("email") || "", [searchParams]);

  const [step, setStep] = useState(1);
  const [email, setEmail] = useState(initialEmail);
  const [loading, setLoading] = useState(false);

  const [emailForm] = Form.useForm();
  const [otpForm] = Form.useForm();

  const handleSendOtp = async () => {
    const values = await emailForm.validateFields();
    setLoading(true);
    const normalizedEmail = values.email.trim().toLowerCase();
    const res = await authService.forgotPassword(normalizedEmail);
    setLoading(false);

    if (!isSuccessResponse(res)) {
      toast.error(getResponseMessage(res, "Failed to send OTP"));
      return;
    }

    setEmail(normalizedEmail);
    setStep(2);
    toast.success(getResponseMessage(res, "OTP sent successfully"));
  };

  const handleVerifyOtp = async () => {
    const values = await otpForm.validateFields();
    setLoading(true);
    const res = await authService.verifyOtp({ email, otp: values.otp.trim() });
    setLoading(false);

    if (!isSuccessResponse(res)) {
      toast.error(getResponseMessage(res, "OTP verification failed"));
      return;
    }

    const nextResetToken = res?.data?.resetToken;
    if (!nextResetToken) {
      toast.error("Reset token is missing");
      return;
    }

    toast.success(getResponseMessage(res, "OTP verified successfully"));
    navigate(`/reset-password?resetToken=${encodeURIComponent(nextResetToken)}`, { replace: true });
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6" style={AUTH_BG_STYLE}>
      <div className="w-full max-w-lg rounded-2xl p-8 glass-surface page-surface" style={AUTH_CARD_STYLE}>
        <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 6, color: "#0A0A0A" }}>Forgot password</h2>
        <p style={{ color: "#595959", marginBottom: 20 }}>Complete 2 steps to verify your account before resetting your password.</p>

        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          description={step === 1 ? "Step 1/2: Enter your email" : "Step 2/2: Enter OTP"}
        />

        {step === 1 && (
          <Form form={emailForm} layout="vertical" initialValues={{ email: initialEmail }}>
            <Form.Item
              label="Email"
              name="email"
              rules={[
                { required: true, message: "Email is required" },
                { type: "email", message: "Invalid email format" },
              ]}
            >
              <Input placeholder="you@company.com" />
            </Form.Item>
            <Button type="primary" style={AUTH_ACTION_PRIMARY_STYLE} loading={loading} onClick={handleSendOtp}>Send OTP</Button>
          </Form>
        )}

        {step === 2 && (
          <>
            <Alert style={{ marginBottom: 12 }} type="success" showIcon description={`OTP sent to ${email}`} />
            <Form form={otpForm} layout="vertical">
              <Form.Item
                label="OTP"
                name="otp"
                rules={[{ required: true, message: "OTP is required" }]}
              >
                <Input placeholder="Enter OTP" />
              </Form.Item>
              <div className="flex gap-2">
                <Button style={AUTH_ACTION_SECONDARY_STYLE} onClick={() => setStep(1)}>Back</Button>
                <Button type="primary" style={AUTH_ACTION_PRIMARY_STYLE} loading={loading} onClick={handleVerifyOtp}>Verify OTP</Button>
              </div>
            </Form>
          </>
        )}

        <div className="mt-5">
          <Button type="link" style={{ paddingLeft: 0 }} onClick={() => navigate("/login")}>Back to login</Button>
        </div>
      </div>
    </div>
  );
}


import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { Checkbox, Form, Input } from "antd";
import { useAuth } from "../context/AuthContext";
import authService from "../api/authService";
import { isAdminRole, isDepartmentManagerRole, isOfficeManagerRole } from "@/utils/role";
import { AUTH_BG_STYLE, AUTH_CARD_STYLE, AUTH_LINK_BUTTON_STYLE, AUTH_OUTLINE_BUTTON_STYLE } from "../constants/authUi";

function defaultHomePath(user) {
  if (isAdminRole(user?.role) || isOfficeManagerRole(user?.role) || isDepartmentManagerRole(user?.role)) return "/departments";
  return "/profile";
}

export function Login() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { isAuthenticated, user, login } = useAuth();
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const code = searchParams.get("code");
    const error = searchParams.get("error");
    if (!code && !error) return;

    const query = searchParams.toString();
    navigate(`/auth/callback${query ? `?${query}` : ""}`, { replace: true });
  }, [navigate, searchParams]);

  useEffect(() => {
    if (!isAuthenticated || !user) return;
    navigate(defaultHomePath(user), { replace: true });
  }, [isAuthenticated, user, navigate]);

  const handleSubmit = async (values) => {
    setSubmitting(true);
    form.setFields([{ name: "password", errors: [] }]);
    try {
      const result = await login({
        email: values.email.trim(),
        password: values.password,
      });

      if (!result?.ok) {
        const msg = result?.message || "Invalid email or password";
        toast.error(msg);
        form.setFields([{ name: "password", errors: [msg] }]);
        return;
      }

      toast.success(result.message || "Login successful");
      navigate(defaultHomePath(result.data), { replace: true });
    } catch {
      toast.error("Unable to login. Please check your connection.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = authService.getGoogleLoginUrl();
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6" style={AUTH_BG_STYLE}>
      <div className="w-full max-w-md rounded-2xl p-8 glass-surface page-surface" style={AUTH_CARD_STYLE}>
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-2">
            <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ background: "linear-gradient(135deg, #5b7cff 0%, #7a5cff 100%)" }}>
              <span style={{ color: "#FFFFFF", fontSize: "20px", fontWeight: "700", fontFamily: "DM Sans, sans-serif" }}>H</span>
            </div>
            <span style={{ fontSize: "24px", fontWeight: "700", fontFamily: "DM Sans, sans-serif", color: "#0A0A0A" }}>HRM System</span>
          </div>
          <p style={{ color: "#595959", fontSize: "14px" }}>Sign in to continue using the system</p>
        </div>

        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
          onFinish={handleSubmit}
          initialValues={{ remember: false }}
          disabled={submitting}
          validateMessages={{
            required: "${label} is required",
            types: { email: "Please enter a valid email" },
          }}
        >
          <Form.Item
            label={<span style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 500 }}>Email</span>}
            name="email"
            rules={[
              { required: true, message: "Please enter your email" },
              { type: "email", message: "Please enter a valid email" },
            ]}
          >
            <Input placeholder="you@company.com" size="middle" style={{ borderColor: "#E8E8E8", fontSize: "14px" }} autoComplete="email" />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 500 }}>Password</span>}
            name="password"
            rules={[{ required: true, message: "Please enter your password" }]}
            validateTrigger={["onSubmit", "onChange"]}
          >
            <Input.Password
              placeholder="********"
              size="middle"
              style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
              autoComplete="current-password"
              onChange={() => form.setFields([{ name: "password", errors: [] }])}
            />
          </Form.Item>

          <Form.Item name="remember" valuePropName="checked" style={{ marginBottom: 20 }}>
            <div className="flex items-center gap-2">
              <Checkbox style={{ margin: 0 }} />
              <span style={{ color: "#595959", fontSize: "13px" }}>Remember me</span>
            </div>
          </Form.Item>

          <div style={{ marginTop: -8, marginBottom: 16, textAlign: "right" }}>
            <button type="button" onClick={() => navigate("/forgot-password")} disabled={submitting} style={AUTH_LINK_BUTTON_STYLE}>
              Forgot password?
            </button>
          </div>

          <button type="submit" disabled={submitting} className="w-full btn-primary-gradient justify-center">
            {submitting ? "Signing in..." : "Sign in"}
          </button>

          <button type="button" onClick={handleGoogleLogin} disabled={submitting} className="w-full mt-3" style={AUTH_OUTLINE_BUTTON_STYLE}>
            Continue with Google
          </button>
        </Form>
      </div>
    </div>
  );
}

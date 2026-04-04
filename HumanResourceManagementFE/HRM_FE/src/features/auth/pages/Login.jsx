import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { useAuth } from "../context/AuthContext";
import { isAdminRole, isOfficeManagerRole, isDepartmentManagerRole } from "@/utils/role";
import { Checkbox, Form, Input } from "antd";

function defaultHomePath(user) {
  if (isAdminRole(user?.role) || isOfficeManagerRole(user?.role) || isDepartmentManagerRole(user?.role)) return "/departments";
  return "/profile";
}

export function Login() {
  const navigate = useNavigate();
  const { isAuthenticated, user, login } = useAuth();
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

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

  return (
    <div
      className="min-h-screen flex items-center justify-center p-6"
      style={{
        background:
          "radial-gradient(circle at 15% 18%, rgba(91,124,255,0.2), transparent 42%), radial-gradient(circle at 82% 6%, rgba(53,195,255,0.16), transparent 36%), #F4F7FF",
      }}
    >
      <div
        className="w-full max-w-md rounded-2xl p-8 glass-surface page-surface"
        style={{ boxShadow: "0 18px 40px rgba(40,62,122,0.16)" }}
      >
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-2">
            <div
              className="w-10 h-10 rounded-lg flex items-center justify-center"
              style={{ background: "linear-gradient(135deg, #5b7cff 0%, #7a5cff 100%)" }}
            >
              <span
                style={{
                  color: "#FFFFFF",
                  fontSize: "20px",
                  fontWeight: "700",
                  fontFamily: "DM Sans, sans-serif",
                }}
              >
                H
              </span>
            </div>
            <span
              style={{
                fontSize: "24px",
                fontWeight: "700",
                fontFamily: "DM Sans, sans-serif",
                color: "#0A0A0A",
              }}
            >
              HRM System
            </span>
          </div>
          <p style={{ color: "#595959", fontSize: "14px" }}>
            Sign in to your account to continue
          </p>
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
            types: { email: "Please enter a valid email address" },
          }}
        >
          <Form.Item
            label={<span style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 500 }}>Email Address</span>}
            name="email"
            rules={[
              { required: true, message: "Please enter your email" },
              { type: "email", message: "Please enter a valid email address" },
            ]}
          >
            <Input
              placeholder="you@company.com"
              size="middle"
              style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
              autoComplete="email"
            />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: 500 }}>Password</span>}
            name="password"
            rules={[{ required: true, message: "Please enter your password" }]}
            validateTrigger={["onSubmit", "onChange"]}
          >
            <Input.Password
              placeholder="••••••••"
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

          <button
            type="submit"
            disabled={submitting}
            className="w-full btn-primary-gradient justify-center"
          >
            {submitting ? "Signing in..." : "Sign In"}
          </button>
        </Form>
      </div>
    </div>
  );
}

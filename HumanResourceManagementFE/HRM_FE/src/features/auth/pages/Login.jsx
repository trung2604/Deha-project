import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { useAuth } from "../context/AuthContext";
import authService from "../api/authService";
import { isAdminRole, isOfficeManagerRole, isDepartmentManagerRole } from "@/utils/role";
import { Checkbox, Form, Input } from "antd";

function defaultHomePath(user) {
  if (isAdminRole(user?.role) || isOfficeManagerRole(user?.role) || isDepartmentManagerRole(user?.role)) return "/departments";
  return "/profile";
}

function mapOAuthError(error) {
  switch (error) {
    case "account_not_found":
      return "Tài khoản Google của bạn chưa được admin cấp trong hệ thống.";
    case "account_inactive":
      return "Tài khoản của bạn đang bị vô hiệu hóa.";
    case "oauth2_failed":
      return "Đăng nhập Google thất bại. Vui lòng thử lại.";
    case "oauth2_email_missing":
      return "Không lấy được email từ Google account.";
    default:
      return "Đăng nhập Google thất bại.";
  }
}

export function Login() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { isAuthenticated, user, login, exchangeOAuth2Code } = useAuth();
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const [oauthProcessing, setOauthProcessing] = useState(false);

  useEffect(() => {
    let active = true;

    const runOAuthCallback = async () => {
      const error = searchParams.get("error");
      const code = searchParams.get("code");
      if (!error && !code) return;

      setOauthProcessing(true);

      if (error) {
        toast.error(mapOAuthError(error));
        if (active) {
          navigate("/login", { replace: true });
          setOauthProcessing(false);
        }
        return;
      }

      if (!code) {
        toast.error("Thiếu mã xác thực OAuth2.");
        if (active) {
          navigate("/login", { replace: true });
          setOauthProcessing(false);
        }
        return;
      }

      const result = await exchangeOAuth2Code(code);
      if (!active) return;

      if (!result?.ok) {
        toast.error(result?.message || "Đăng nhập Google thất bại");
        navigate("/login", { replace: true });
        setOauthProcessing(false);
        return;
      }

      toast.success(result.message || "Đăng nhập Google thành công");
      navigate(defaultHomePath(result.data), { replace: true });
      setOauthProcessing(false);
    };

    runOAuthCallback();

    return () => {
      active = false;
    };
  }, [searchParams, exchangeOAuth2Code, navigate]);

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
          disabled={submitting || oauthProcessing}
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
            disabled={submitting || oauthProcessing}
            className="w-full btn-primary-gradient justify-center"
          >
            {submitting ? "Signing in..." : oauthProcessing ? "Processing Google sign-in..." : "Sign In"}
          </button>

          <button
            type="button"
            onClick={handleGoogleLogin}
            disabled={submitting || oauthProcessing}
            className="w-full mt-3"
            style={{
              height: "38px",
              borderRadius: "10px",
              border: "1px solid #E8E8E8",
              background: "#FFFFFF",
              color: "#0A0A0A",
              fontSize: "14px",
              fontWeight: 500,
            }}
          >
            Continue with Google
          </button>
        </Form>
      </div>
    </div>
  );
}

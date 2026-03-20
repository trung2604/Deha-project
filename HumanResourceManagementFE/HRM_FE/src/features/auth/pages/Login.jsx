import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { useAuth } from "../context/AuthContext";
import { Checkbox, Input } from "antd";

function defaultHomePath(user) {
  const role = user?.role;
  if (role === "ADMIN" || role === "ROLE_ADMIN") return "/users";
  return "/profile";
}

export function Login() {
  const navigate = useNavigate();
  const { isAuthenticated, user, login } = useAuth();
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    remember: false,
  });

  useEffect(() => {
    if (!isAuthenticated || !user) return;
    navigate(defaultHomePath(user), { replace: true });
  }, [isAuthenticated, user, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const result = await login({
        email: formData.email.trim(),
        password: formData.password,
      });

      if (!result?.ok) {
        toast.error(result?.message || "Login failed");
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
      style={{ backgroundColor: "#F5F7FA" }}
    >
      <div
        className="w-full max-w-md rounded-xl p-8"
        style={{ backgroundColor: "#FFFFFF", boxShadow: "0 4px 24px rgba(0,0,0,0.08)" }}
      >
        {/* Logo & Title */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-2">
            <div
              className="w-10 h-10 rounded-lg flex items-center justify-center"
              style={{ backgroundColor: "#1677FF" }}
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

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label
              className="block mb-1.5"
              style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: "500" }}
            >
              Email Address
            </label>
            <Input
              type="email"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              disabled={submitting}
              style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
              size="middle"
              placeholder="you@company.com"
            />
          </div>

          <div>
            <label
              className="block mb-1.5"
              style={{ color: "#0A0A0A", fontSize: "13px", fontWeight: "500" }}
            >
              Password
            </label>
            <Input.Password
              required
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              disabled={submitting}
              style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
              placeholder="••••••••"
              size="middle"
            />
          </div>

          <div className="flex items-center justify-start">
            <div className="flex items-center gap-2">
              <Checkbox
                checked={formData.remember}
                onChange={(e) =>
                  setFormData({ ...formData, remember: e.target.checked })
                }
                disabled={submitting}
                style={{ margin: 0 }}
              />
              <span style={{ color: "#595959", fontSize: "13px" }}>Remember me</span>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full h-10 rounded-lg transition-all duration-150 hover:opacity-90"
            style={{
              backgroundColor: "#1677FF",
              color: "#FFFFFF",
              fontSize: "14px",
              fontWeight: "600",
            }}
          >
            {submitting ? "Signing in..." : "Sign In"}
          </button>
        </form>

      </div>
    </div>
  );
}

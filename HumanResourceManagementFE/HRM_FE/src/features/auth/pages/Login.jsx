import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "../context/AuthContext";

function defaultHomePath(user) {
  const role = user?.role;
  if (role === "ADMIN" || role === "ROLE_ADMIN") return "/users";
  return "/profile";
}

export function Login() {
  const navigate = useNavigate();
  const { isAuthenticated, user, login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
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
            <input
              type="email"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              disabled={submitting}
              className="w-full h-10 px-3 rounded-lg border outline-none transition-all duration-150 focus:border-blue-500"
              style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
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
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                required
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                disabled={submitting}
                className="w-full h-10 px-3 pr-10 rounded-lg border outline-none transition-all duration-150 focus:border-blue-500"
                style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
                placeholder="••••••••"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                disabled={submitting}
                className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-gray-100 rounded transition-colors"
              >
                {showPassword ? (
                  <EyeOff className="w-4 h-4" style={{ color: "#595959" }} />
                ) : (
                  <Eye className="w-4 h-4" style={{ color: "#595959" }} />
                )}
              </button>
            </div>
          </div>

          <div className="flex items-center justify-start">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={formData.remember}
                onChange={(e) => setFormData({ ...formData, remember: e.target.checked })}
                disabled={submitting}
                className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                style={{ accentColor: "#1677FF" }}
              />
              <span style={{ color: "#595959", fontSize: "13px" }}>
                Remember me
              </span>
            </label>
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

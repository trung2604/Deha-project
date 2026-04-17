import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { useAuth } from "../context/AuthContext";
import { isAdminRole, isDepartmentManagerRole, isOfficeManagerRole } from "@/utils/role";

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

export function OAuth2Callback() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { exchangeOAuth2Code } = useAuth();

  useEffect(() => {
    let active = true;

    const run = async () => {
      const error = searchParams.get("error");
      if (error) {
        toast.error(mapOAuthError(error));
        navigate("/login", { replace: true });
        return;
      }

      const code = searchParams.get("code");
      if (!code) {
        toast.error("Missing OAuth2 code");
        navigate("/login", { replace: true });
        return;
      }

      const result = await exchangeOAuth2Code(code);
      if (!active) return;

      if (!result?.ok) {
        toast.error(result?.message || "OAuth2 login failed");
        navigate("/login", { replace: true });
        return;
      }

      toast.success(result.message || "Login successful");
      navigate(defaultHomePath(result.data), { replace: true });
    };

    run();
    return () => {
      active = false;
    };
  }, [exchangeOAuth2Code, navigate, searchParams]);

  return (
    <div className="min-h-screen flex items-center justify-center p-6" style={{ background: "#F4F7FF" }}>
      <div className="rounded-2xl p-8 glass-surface page-surface" style={{ minWidth: "320px" }}>
        <p style={{ margin: 0, color: "#595959", fontSize: "14px" }}>Completing Google sign-in...</p>
      </div>
    </div>
  );
}


import { Navigate } from "react-router-dom";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole } from "@/utils/role";

/** Default landing: admin → users list, employee → profile */
export function IndexRedirect() {
  const { user } = useAuth();
  if (isAdminRole(user?.role)) {
    return <Navigate to="/users" replace />;
  }
  return <Navigate to="/profile" replace />;
}

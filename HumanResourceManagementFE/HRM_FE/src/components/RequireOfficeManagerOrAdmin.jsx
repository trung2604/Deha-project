import { Navigate } from "react-router-dom";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole, isOfficeManagerRole } from "@/utils/role";

export function RequireOfficeManagerOrAdmin({ children }) {
  const { user, initializing } = useAuth();

  if (initializing) {
    return (
      <div className="min-h-[40vh] flex items-center justify-center">
        <div
          className="w-10 h-10 rounded-full border-2 border-t-transparent animate-spin"
          style={{ borderColor: "#1677FF", borderTopColor: "transparent" }}
        />
      </div>
    );
  }

  if (!isAdminRole(user?.role) && !isOfficeManagerRole(user?.role)) {
    return <Navigate to="/profile" replace />;
  }

  return children;
}


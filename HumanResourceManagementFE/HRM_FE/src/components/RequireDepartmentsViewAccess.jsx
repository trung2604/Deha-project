import { Navigate } from "react-router-dom";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole, isDepartmentManagerRole, isOfficeManagerRole } from "@/utils/role";

/**
 * Departments page access:
 * - ADMIN: full access (list + manage)
 * - OFFICE_MANAGER: full access (list + manage)
 * - DEPARTMENT_MANAGER: view-only access (manage buttons are hidden in UI)
 */
export function RequireDepartmentsViewAccess({ children }) {
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

  const role = user?.role;
  const allowed =
    isAdminRole(role) || isOfficeManagerRole(role) || isDepartmentManagerRole(role);

  if (!allowed) return <Navigate to="/profile" replace />;

  return children;
}


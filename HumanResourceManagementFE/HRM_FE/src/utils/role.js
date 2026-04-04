/**
 * Canonical frontend role values:
 * ADMIN | MANAGER_OFFICE | MANAGER_DEPARTMENT | EMPLOYEE
 */
export function normalizeRole(role) {
  if (role == null) return "";
  const normalized = String(role).trim().toUpperCase();
  return normalized.startsWith("ROLE_") ? normalized.slice(5) : normalized;
}

export function isAdminRole(role) {
  return normalizeRole(role) === "ADMIN";
}

export function isEmployeeRole(role) {
  return normalizeRole(role) === "EMPLOYEE";
}

export function isOfficeManagerRole(role) {
  return normalizeRole(role) === "MANAGER_OFFICE";
}

export function isDepartmentManagerRole(role) {
  return normalizeRole(role) === "MANAGER_DEPARTMENT";
}

export function isManagerRole(role) {
  return isOfficeManagerRole(role) || isDepartmentManagerRole(role);
}

/**
 * @param {string} itemRoles 
 * @param {string|undefined} userRole 
 */
export function canAccessNavItem(itemRoles, userRole) {
  if (!itemRoles?.length) return true;
  const itemRolesSet = new Set(itemRoles.map(normalizeRole));
  if (isAdminRole(userRole) && itemRolesSet.has("ADMIN")) return true;
  if (isOfficeManagerRole(userRole) && itemRolesSet.has("MANAGER_OFFICE")) return true;
  if (isDepartmentManagerRole(userRole) && itemRolesSet.has("MANAGER_DEPARTMENT")) return true;
  if (isManagerRole(userRole) && itemRolesSet.has("MANAGER")) return true; // legacy
  if (isEmployeeRole(userRole) && itemRolesSet.has("EMPLOYEE")) return true;
  return false;
}

export function formatRoleLabel(role) {
  const normalized = normalizeRole(role);
  if (normalized === "ADMIN") return "Administrator";
  if (normalized === "MANAGER_OFFICE") return "Office Manager";
  if (normalized === "MANAGER_DEPARTMENT") return "Department Manager";
  if (normalized === "EMPLOYEE") return "Employee";
  return normalized || "Employee";
}


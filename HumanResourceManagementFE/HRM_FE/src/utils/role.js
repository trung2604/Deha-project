/**
 * Backend Role enum:
 * ROLE_ADMIN | ROLE_MANAGER_OFFICE | ROLE_MANAGER_DEPARTMENT | ROLE_EMPLOYEE
 */
export function normalizeRole(role) {
  if (role == null) return "";
  return String(role).trim().toUpperCase();
}

export function isAdminRole(role) {
  const r = normalizeRole(role);
  return r === "ROLE_ADMIN" || r === "ADMIN";
}

export function isEmployeeRole(role) {
  const r = normalizeRole(role);
  return r === "ROLE_EMPLOYEE" || r === "EMPLOYEE";
}

export function isOfficeManagerRole(role) {
  const r = normalizeRole(role);
  return r === "ROLE_MANAGER_OFFICE"|| r === "MANAGER_OFFICE";
}

export function isDepartmentManagerRole(role) {
  const r = normalizeRole(role);
  return r === "ROLE_MANAGER_DEPARTMENT" || r === "MANAGER_DEPARTMENT";
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
  if (isAdminRole(userRole) && itemRoles.includes("ADMIN")) return true;
  if (isOfficeManagerRole(userRole) && itemRoles.includes("MANAGER_OFFICE")) return true;
  if (isDepartmentManagerRole(userRole) && itemRoles.includes("MANAGER_DEPARTMENT")) return true;
  if (isManagerRole(userRole) && itemRoles.includes("MANAGER")) return true; // legacy
  if (isEmployeeRole(userRole) && itemRoles.includes("EMPLOYEE")) return true;
  return false;
}

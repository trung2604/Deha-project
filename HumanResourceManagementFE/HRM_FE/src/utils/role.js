/**
 * Backend uses enum Role: ROLE_ADMIN | ROLE_EMPLOYEE (serialized in JSON).
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

/**
 * @param {string} itemRoles 
 * @param {string|undefined} userRole 
 */
export function canAccessNavItem(itemRoles, userRole) {
  if (!itemRoles?.length) return true;
  if (isAdminRole(userRole) && itemRoles.includes("ADMIN")) return true;
  if (isEmployeeRole(userRole) && itemRoles.includes("EMPLOYEE")) return true;
  return false;
}

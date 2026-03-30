export const DEPARTMENT_WORKSPACE_COPY = {
  titleManager: "My Department Workspace",
  titleDefault: "Departments",
  managerDescription:
    "Focused view for department managers: department profile, positions, and team members.",
  managerNotAssignedTitle: "No department is linked to your account",
  managerNotAssignedDescription:
    "Please contact office manager/admin to assign your department.",
  managerViewBadge: "Manager View",
};

export function resolveManagerDepartment(departments, user) {
  if (!Array.isArray(departments) || !user) return null;

  return (
    departments.find((d) => d?.id === user?.departmentId) ||
    departments.find((d) => d?.name && user?.departmentName && d.name === user.departmentName) ||
    null
  );
}

export function getDepartmentCountLabel({
  departmentManager,
  myDepartment,
  visibleDepartments,
  debouncedSearchTerm,
  departmentTotalCount,
}) {
  if (departmentManager) return myDepartment ? "1" : "0";

  if (debouncedSearchTerm) return `${visibleDepartments.length} / ${departmentTotalCount}`;

  return String(departmentTotalCount);
}


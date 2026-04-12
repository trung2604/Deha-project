const ENTITY_LABELS = [
  { pattern: "/users/me/avatar", label: "profile avatar" },
  { pattern: "/users", label: "user" },
  { pattern: "/auth", label: "authentication" },
  { pattern: "/offices", label: "office" },
  { pattern: "/departments", label: "department" },
  { pattern: "/positions", label: "position" },
  { pattern: "/salary-contracts", label: "salary contract" },
  { pattern: "/payrolls", label: "payroll" },
  { pattern: "/ot-requests", label: "overtime request" },
  { pattern: "/ot-reports", label: "overtime report" },
  { pattern: "/ot-sessions", label: "overtime session" },
];

function normalizeText(value) {
  return String(value || "").toLowerCase();
}

export function getAuditMethodLabel(method) {
  const normalized = normalizeText(method);
  if (normalized === "post") return "Submit";
  if (normalized === "put") return "Update";
  if (normalized === "patch") return "Approve/Update status";
  if (normalized === "delete") return "Delete";
  return "Action";
}

export function getAuditActionLabel(httpMethod, endpointPattern) {
  const method = normalizeText(httpMethod);
  const endpoint = normalizeText(endpointPattern);

  if (endpoint.includes("/users/me/avatar")) {
    if (method === "delete") return "Delete profile avatar";
    return "Update profile avatar";
  }

  if (endpoint.includes("/users/") && endpoint.includes("/deactivate")) {
    return "Deactivate user";
  }

  if (endpoint.includes("/users/") && endpoint.includes("/reset-password")) {
    return "Reset user password";
  }

  if (endpoint.endsWith("/users") || endpoint.includes("/users?")) {
    if (method === "post") return "Create user";
    return "Update user";
  }

  if (endpoint.includes("/offices")) {
    if (method === "post") return "Create office";
    if (method === "put") return "Update office";
    if (method === "delete") return "Delete office";
    return "Manage office";
  }

  if (endpoint.includes("/departments")) {
    if (method === "post") return "Create department";
    if (method === "put") return "Update department";
    if (method === "delete") return "Delete department";
    return "Manage department";
  }

  if (endpoint.includes("/positions")) {
    if (method === "post") return "Create position";
    if (method === "put") return "Update position";
    if (method === "delete") return "Delete position";
    return "Manage position";
  }

  if (endpoint.includes("/salary-contracts")) {
    if (method === "post") return "Create salary contract";
    if (method === "put") return "Update salary contract";
    return "Manage salary contract";
  }

  if (endpoint.includes("/payrolls/generate")) {
    return "Generate payroll";
  }

  if (endpoint.includes("/payrolls")) {
    return "Process payroll";
  }

  if (endpoint.includes("/attendance")) {
    return "Update attendance";
  }

  if (endpoint.includes("/ot-requests")) {
    if (method === "post") return "Create overtime request";
    return "Process overtime request";
  }

  if (endpoint.includes("/ot-reports")) {
    if (method === "post") return "Create overtime report";
    return "Process overtime report";
  }

  if (endpoint.includes("/ot-sessions")) {
    if (method === "post") return "Start overtime session";
    return "End overtime session";
  }

  if (endpoint.includes("/notifications")) {
    if (method === "post") return "Create system notification";
    if (method === "patch") return "Mark notification as read";
    if (method === "delete") return "Delete notification";
    return "Update notification";
  }

  if (endpoint.includes("/chat")) {
    return "Chat interaction";
  }

  if (endpoint.includes("/auth")) {
    if (endpoint.includes("/login")) return "Sign in";
    if (endpoint.includes("/refresh")) return "Refresh session";
    if (endpoint.includes("/oauth2/exchange")) return "OAuth2 sign in exchange";
    if (endpoint.includes("/logout")) return "Sign out";
    if (endpoint.includes("/forgot-password")) return "Request password reset";
    if (endpoint.includes("/verify-otp")) return "Verify password reset OTP";
    if (endpoint.includes("/change-password")) return "Change password";
    if (endpoint.includes("/reset-password")) return "Recover password";
    if (endpoint.includes("/me") && method === "put") return "Update my profile";
    return "Account authentication";
  }

  const matchedEntity = ENTITY_LABELS.find((item) => endpoint.includes(item.pattern));
  if (matchedEntity) {
    return `${getAuditMethodLabel(method)} ${matchedEntity.label}`;
  }

  return "Scoped system action";
}

export function getAuditStatusLabel(success) {
  if (success === true) return "Success";
  if (success === false) return "Failed";
  return "Unknown";
}

export function getAuditStatusColor(success) {
  if (success === true) return "green";
  if (success === false) return "red";
  return "default";
}


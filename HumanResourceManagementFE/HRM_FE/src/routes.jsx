/* eslint-disable react-refresh/only-export-components */
import { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";
import { Layout } from "./components/Layout";
import { IndexRedirect } from "./components/IndexRedirect";
import { RequireAdmin } from "./components/RequireAdmin";
import { RequireManagerOrAdmin } from "./components/RequireManagerOrAdmin";
import { RequireOfficeManagerOrAdmin } from "./components/RequireOfficeManagerOrAdmin";
import { RequireOfficeManager } from "./components/RequireOfficeManager";
import { RequireDepartmentsViewAccess } from "./components/RequireDepartmentsViewAccess";
import { RequireOvertimeAccess } from "./components/RequireOvertimeAccess";

const Login = lazy(() => import("@/features/auth/pages/Login").then((m) => ({ default: m.Login })));
const OAuth2Callback = lazy(() => import("@/features/auth/pages/OAuth2Callback").then((m) => ({ default: m.OAuth2Callback })));
const VerifyEmail = lazy(() => import("@/features/auth/pages/VerifyEmail").then((m) => ({ default: m.VerifyEmail })));
const ForgotPassword = lazy(() => import("@/features/auth/pages/ForgotPassword").then((m) => ({ default: m.ForgotPassword })));
const ResetPassword = lazy(() => import("@/features/auth/pages/ResetPassword").then((m) => ({ default: m.ResetPassword })));
const UsersPage = lazy(() => import("@/features/users/pages/UserPage").then((m) => ({ default: m.UsersPage })));
const DepartmentsPage = lazy(() =>
  import("@/features/departments/pages/DepartmentsPage").then((m) => ({ default: m.DepartmentsPage })),
);
const DepartmentDetailPage = lazy(() =>
  import("@/features/departments/pages/DepartmentDetailPage").then((m) => ({ default: m.DepartmentDetailPage })),
);
const OfficesPage = lazy(() => import("@/features/offices/pages/OfficesPage").then((m) => ({ default: m.OfficesPage })));
const OfficePolicyPage = lazy(() =>
  import("@/features/offices/pages/OfficePolicyPage").then((m) => ({ default: m.OfficePolicyPage })),
);
const AttendancePage = lazy(() =>
  import("@/features/attendance/pages/AttendancePage").then((m) => ({ default: m.AttendancePage })),
);
const OvertimePage = lazy(() =>
  import("@/features/overtime/pages/OvertimePage").then((m) => ({ default: m.OvertimePage })),
);
const PayrollPage = lazy(() => import("@/features/payroll/pages/PayrollPage").then((m) => ({ default: m.PayrollPage })));
const Profile = lazy(() => import("@/features/profile/pages/Profile"));
const ChatPage = lazy(() => import("@/features/chat/pages/ChatPage.jsx").then((m) => ({ default: m.ChatPage })));
const NotificationsPage = lazy(() =>
  import("@/features/notifications/pages/NotificationsPage").then((m) => ({ default: m.NotificationsPage })),
);

function RouteLoader() {
  return (
    <div className="rounded-xl p-6 glass-surface page-surface soft-ring">
      <p style={{ margin: 0, color: "#8C8C8C", fontSize: "14px" }}>Loading page...</p>
    </div>
  );
}

function withSuspense(element) {
  return <Suspense fallback={<RouteLoader />}>{element}</Suspense>;
}
// import { Attendance } from "./pages/Attendance";
// import { LeaveRequests } from "./pages/LeaveRequests";
// import { Salary } from "./pages/Salary";
// import { ActivityLogs } from "./pages/ActivityLogs";
// import { Register } from "./pages/Register";
// import Profile from "./pages/Profile";
// import { Notifications } from "./pages/Notifications";

export const router = createBrowserRouter([
  // Auth routes (no layout)
  {
    path: "/login",
    element: withSuspense(<Login />),
  },
  {
    path: "/auth/callback",
    element: withSuspense(<OAuth2Callback />),
  },
  {
    path: "/verify-email",
    element: withSuspense(<VerifyEmail />),
  },
  {
    path: "/forgot-password",
    element: withSuspense(<ForgotPassword />),
  },
  {
    path: "/reset-password",
    element: withSuspense(<ResetPassword />),
  },
//   {
//     path: "/register",
//     Component: Register,
//   },
  // Main app routes (with layout)
  {
    path: "/",
    Component: Layout,
    children: [
      { index: true, Component: IndexRedirect },
      {
        path: "offices",
        element: (
          <RequireAdmin>{withSuspense(<OfficesPage />)}</RequireAdmin>
        ),
      },
      {
        path: "office-policy",
        element: (
          <RequireOfficeManager>{withSuspense(<OfficePolicyPage />)}</RequireOfficeManager>
        ),
      },
      {
        path: "users",
        element: (
          <RequireManagerOrAdmin>{withSuspense(<UsersPage />)}</RequireManagerOrAdmin>
        ),
      },
      {
        path: "departments",
        element: (
          <RequireDepartmentsViewAccess>{withSuspense(<DepartmentsPage />)}</RequireDepartmentsViewAccess>
        ),
      },
      {
        path: "departments/:departmentId",
        element: (
          <RequireDepartmentsViewAccess>{withSuspense(<DepartmentDetailPage />)}</RequireDepartmentsViewAccess>
        ),
      },
      {
        path: "attendance",
        element: withSuspense(<AttendancePage />),
      },
      {
        path: "overtime",
        element: (
          <RequireOvertimeAccess>{withSuspense(<OvertimePage />)}</RequireOvertimeAccess>
        ),
      },
      {
        path: "payroll",
        element: (
          <RequireOfficeManagerOrAdmin>{withSuspense(<PayrollPage />)}</RequireOfficeManagerOrAdmin>
        ),
      },
      {
        path: "chat",
        element: withSuspense(<ChatPage />),
      },
      {
        path: "notifications",
        element: withSuspense(<NotificationsPage />),
      },
    //   { path: "leave-requests", Component: LeaveRequests },
    //   { path: "salary", Component: Salary },
    //   { path: "activity-logs", Component: ActivityLogs },
      { path: "profile", element: withSuspense(<Profile />) },
    ],
  },
]);
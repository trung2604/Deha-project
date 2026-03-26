import { createBrowserRouter } from "react-router-dom";
import { Layout } from "./components/Layout";
import { IndexRedirect } from "./components/IndexRedirect";
import { RequireAdmin } from "./components/RequireAdmin";
import { RequireManagerOrAdmin } from "./components/RequireManagerOrAdmin";
import { RequireOfficeManagerOrAdmin } from "./components/RequireOfficeManagerOrAdmin";
import { RequireDepartmentsViewAccess } from "./components/RequireDepartmentsViewAccess";
import { UsersPage } from "@/features/users/pages/UserPage";
import { DepartmentsPage } from "@/features/departments/pages/DepartmentsPage";
import { DepartmentDetailPage } from "@/features/departments/pages/DepartmentDetailPage";
import { OfficesPage } from "@/features/offices/pages/OfficesPage";
import { AttendancePage } from "@/features/attendance/pages/AttendancePage";
import { PayrollPage } from "@/features/payroll/pages/PayrollPage";
import { Login } from "@/features/auth/pages/Login";
import Profile from "@/features/profile/pages/Profile";
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
    Component: Login,
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
          <RequireAdmin>
            <OfficesPage />
          </RequireAdmin>
        ),
      },
      {
        path: "users",
        element: (
          <RequireManagerOrAdmin>
            <UsersPage />
          </RequireManagerOrAdmin>
        ),
      },
      {
        path: "departments",
        element: (
          <RequireDepartmentsViewAccess>
            <DepartmentsPage />
          </RequireDepartmentsViewAccess>
        ),
      },
      {
        path: "departments/:departmentId",
        element: (
          <RequireDepartmentsViewAccess>
            <DepartmentDetailPage />
          </RequireDepartmentsViewAccess>
        ),
      },
      {
        path: "attendance",
        element: <AttendancePage />,
      },
      {
        path: "payroll",
        element: (
          <RequireOfficeManagerOrAdmin>
            <PayrollPage />
          </RequireOfficeManagerOrAdmin>
        ),
      },
    //   { path: "leave-requests", Component: LeaveRequests },
    //   { path: "salary", Component: Salary },
    //   { path: "activity-logs", Component: ActivityLogs },
      { path: "profile", Component: Profile },
    //   { path: "notifications", Component: Notifications },
    ],
  },
]);
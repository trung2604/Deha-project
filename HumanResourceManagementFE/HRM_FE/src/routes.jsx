import { createBrowserRouter } from "react-router-dom";
import { Layout } from "./components/Layout";
import { IndexRedirect } from "./components/IndexRedirect";
import { RequireAdmin } from "./components/RequireAdmin";
import { UsersPage } from "@/features/users/pages/UserPage";
import { DepartmentsPage } from "@/features/departments/pages/DepartmentsPage";
import { DepartmentDetailPage } from "@/features/departments/pages/DepartmentDetailPage";
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
        path: "users",
        element: (
          <RequireAdmin>
            <UsersPage />
          </RequireAdmin>
        ),
      },
      {
        path: "departments",
        element: (
          <RequireAdmin>
            <DepartmentsPage />
          </RequireAdmin>
        ),
      },
      {
        path: "departments/:departmentId",
        element: (
          <RequireAdmin>
            <DepartmentDetailPage />
          </RequireAdmin>
        ),
      },
    //   { path: "attendance", Component: Attendance },
    //   { path: "leave-requests", Component: LeaveRequests },
    //   { path: "salary", Component: Salary },
    //   { path: "activity-logs", Component: ActivityLogs },
      { path: "profile", Component: Profile },
    //   { path: "notifications", Component: Notifications },
    ],
  },
]);
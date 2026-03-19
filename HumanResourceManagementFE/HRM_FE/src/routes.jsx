import { createBrowserRouter } from "react-router-dom";
import { Layout } from "./components/Layout";
// import { Dashboard } from "./pages/Dashboard";
import { EmployeesPage } from "@/features/employees/pages/EmployeesPage";
import { DepartmentsPage } from "@/features/departments/pages/DepartmentsPage";
import { DepartmentDetailPage } from "@/features/departments/pages/DepartmentDetailPage";
// import { Attendance } from "./pages/Attendance";
// import { LeaveRequests } from "./pages/LeaveRequests";
// import { Salary } from "./pages/Salary";
// import { ActivityLogs } from "./pages/ActivityLogs";
// import { Login } from "./pages/Login";
// import { Register } from "./pages/Register";
// import Profile from "./pages/Profile";
// import { Notifications } from "./pages/Notifications";

export const router = createBrowserRouter([
  // Auth routes (no layout)
//   {
//     path: "/login",
//     Component: Login,
//   },
//   {
//     path: "/register",
//     Component: Register,
//   },
  // Main app routes (with layout)
  {
    path: "/",
    Component: Layout,
    children: [
    //   { index: true, Component: Dashboard },
      { path: "employees", Component: EmployeesPage },
      { path: "departments", Component: DepartmentsPage },
      { path: "departments/:departmentId", Component: DepartmentDetailPage },
    //   { path: "attendance", Component: Attendance },
    //   { path: "leave-requests", Component: LeaveRequests },
    //   { path: "salary", Component: Salary },
    //   { path: "activity-logs", Component: ActivityLogs },
    //   { path: "profile", Component: Profile },
    //   { path: "notifications", Component: Notifications },
    ],
  },
]);
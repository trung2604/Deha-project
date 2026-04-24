import { useEffect, useMemo, useState } from "react";
import { Activity, AlertTriangle, BarChart3, Building2, CheckCircle2, Eye, PieChart, Shield, Sparkles, UserCircle, Users2, Wallet } from "lucide-react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Button, Drawer, Select, Tag } from "antd";
import { useAuth } from "@/features/auth/context/AuthContext";
import auditLogService from "@/features/auditLogs/api/auditLogService";
import userService from "@/features/users/api/UserService";
import departmentService from "@/features/departments/api/departmentService";
import officeService from "@/features/offices/api/officeService";
import attendanceService from "@/features/attendance/api/attendanceService";
import overtimeService from "@/features/overtime/api/overtimeService";
import payrollService from "@/features/payroll/api/payrollService";
import { getAuditActionLabel, getAuditStatusColor, getAuditStatusLabel } from "@/features/auditLogs/utils/auditLogDisplay";
import { getDepartmentDirectoryPayload, getPageMeta, getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";
import { formatRoleLabel, isAdminRole, isManagerRole, isOfficeManagerRole } from "@/utils/role";

export function DashboardPage() {
  const { user } = useAuth();
  const [loadingMetrics, setLoadingMetrics] = useState(false);
  const [metrics, setMetrics] = useState({ total: 0, failed: 0, success: 0 });
  const [recentLogs, setRecentLogs] = useState([]);
  const [recent24hLogs, setRecent24hLogs] = useState([]);
  const [activitySnapshot, setActivitySnapshot] = useState({
    total24h: 0,
    failed24h: 0,
    uniqueActors24h: 0,
    latestActor: "--",
    latestAt: null,
  });
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);
  const [lastUpdatedAt, setLastUpdatedAt] = useState(null);
  const [loadingSystemMetrics, setLoadingSystemMetrics] = useState(false);
  const [selectedOfficeId, setSelectedOfficeId] = useState(undefined);
  const [officeOptions, setOfficeOptions] = useState([]);
  const [systemMetrics, setSystemMetrics] = useState({
    totalUsers: 0,
    totalDepartments: 0,
    totalOffices: 0,
    payrollRowsThisMonth: 0,
    pendingOtItems: 0,
    attendanceCheckedIn: 0,
    attendanceCheckedOut: 0,
    attendanceOpen: 0,
    otPendingRequests: 0,
    otPendingReports: 0,
    otApprovedReports: 0,
    payrollDraft: 0,
    payrollFinalized: 0,
    payrollPaid: 0,
  });

  const canViewAuditMetrics = useMemo(
    () => isAdminRole(user?.role) || isOfficeManagerRole(user?.role),
    [user?.role],
  );

  const canViewApprovalScope = useMemo(
    () => isAdminRole(user?.role) || isManagerRole(user?.role),
    [user?.role],
  );

  const actorOfficeId = useMemo(
    () => user?.officeId || user?.office?.id || undefined,
    [user?.office?.id, user?.officeId],
  );

  const actorOfficeName = useMemo(
    () => user?.officeName || user?.office?.name || "My office",
    [user?.office?.name, user?.officeName],
  );

  const scopedOfficeId = useMemo(
    () => (isOfficeManagerRole(user?.role) ? actorOfficeId : selectedOfficeId),
    [actorOfficeId, selectedOfficeId, user?.role],
  );

  useEffect(() => {
    const loadSystemMetrics = async () => {
      setLoadingSystemMetrics(true);
      try {
        const now = new Date();
        const [
          usersResult,
          departmentsResult,
          officesResult,
          payrollResult,
          officeAttendanceResult,
          otRequestsResult,
          otReportsResult,
        ] = await Promise.allSettled([
          userService.getUsers({ page: 0, size: 1, officeId: scopedOfficeId }),
          departmentService.getDepartments(),
          isAdminRole(user?.role)
            ? officeService.getOffices()
            : Promise.resolve({ status: 200, data: actorOfficeId ? [{ id: actorOfficeId, name: actorOfficeName }] : [] }),
          payrollService.listPayrolls({ year: now.getFullYear(), month: now.getMonth() + 1, officeId: scopedOfficeId }),
          attendanceService.getOfficeToday({ officeId: scopedOfficeId }),
          canViewApprovalScope ? overtimeService.getOvertimeRequestsByApprovalScope() : overtimeService.getMyOvertimeRequests(),
          canViewApprovalScope ? overtimeService.getOvertimeReportsByApprovalScope() : overtimeService.getMyOvertimeReports(),
        ]);

        const usersRes = usersResult.status === "fulfilled" ? usersResult.value : null;
        const departmentsRes = departmentsResult.status === "fulfilled" ? departmentsResult.value : null;
        const officesRes = officesResult.status === "fulfilled" ? officesResult.value : null;
        const payrollRes = payrollResult.status === "fulfilled" ? payrollResult.value : null;
        const officeAttendanceRes = officeAttendanceResult.status === "fulfilled" ? officeAttendanceResult.value : null;
        const otRequestsRes = otRequestsResult.status === "fulfilled" ? otRequestsResult.value : null;
        const otReportsRes = otReportsResult.status === "fulfilled" ? otReportsResult.value : null;

        const usersTotal = isSuccessResponse(usersRes) ? getPageMeta(usersRes).totalElements : 0;
        const departmentsPayload = getDepartmentDirectoryPayload(departmentsRes);
        const officesData = isSuccessResponse(officesRes) && Array.isArray(officesRes?.data) ? officesRes.data : [];
        const officesTotal = officesData.length;
        setOfficeOptions(officesData.map((office) => ({ value: office.id, label: office.name })));
        const payrollRows = isSuccessResponse(payrollRes) && Array.isArray(payrollRes?.data) ? payrollRes.data : [];
        const officeAttendanceRows = isSuccessResponse(officeAttendanceRes) && Array.isArray(officeAttendanceRes?.data)
          ? officeAttendanceRes.data
          : [];
        const otRequests = isSuccessResponse(otRequestsRes) && Array.isArray(otRequestsRes?.data) ? otRequestsRes.data : [];
        const otReports = isSuccessResponse(otReportsRes) && Array.isArray(otReportsRes?.data) ? otReportsRes.data : [];
        const payrollDraft = payrollRows.filter((item) => String(item?.status || "").toUpperCase() === "DRAFT").length;
        const payrollFinalized = payrollRows.filter((item) => String(item?.status || "").toUpperCase() === "FINALIZED").length;
        const payrollPaid = payrollRows.filter((item) => String(item?.status || "").toUpperCase() === "PAID").length;
        const otPendingRequests = otRequests.filter((item) => String(item?.status || "").toUpperCase() === "PENDING").length;
        const otPendingReports = otReports.filter((item) => String(item?.status || "").toUpperCase() === "PENDING").length;
        const otApprovedReports = otReports.filter((item) => String(item?.status || "").toUpperCase() === "APPROVED").length;
        const attendanceCheckedIn = officeAttendanceRows.filter((item) => Boolean(item?.checkInTime)).length;
        const attendanceCheckedOut = officeAttendanceRows.filter((item) => Boolean(item?.checkOutTime)).length;
        const attendanceOpen = Math.max(0, attendanceCheckedIn - attendanceCheckedOut);
        const pendingOtCount = otPendingRequests + otPendingReports;

        setSystemMetrics({
          totalUsers: usersTotal,
          totalDepartments: departmentsPayload.totalCount,
          totalOffices: officesTotal,
          payrollRowsThisMonth: payrollRows.length,
          pendingOtItems: pendingOtCount,
          attendanceCheckedIn,
          attendanceCheckedOut,
          attendanceOpen,
          otPendingRequests,
          otPendingReports,
          otApprovedReports,
          payrollDraft,
          payrollFinalized,
          payrollPaid,
        });
      } finally {
        setLoadingSystemMetrics(false);
      }
    };

    loadSystemMetrics();
  }, [actorOfficeId, actorOfficeName, canViewApprovalScope, scopedOfficeId, user?.role]);

  useEffect(() => {
    if (!canViewAuditMetrics) return;

    const loadMetrics = async () => {
      setLoadingMetrics(true);
      try {
        const since24h = new Date();
        since24h.setHours(since24h.getHours() - 24);
        const since24hIso = since24h.toISOString();

        const [allRes, failedRes, successRes, recentRes, recent24hRes] = await Promise.all([
          auditLogService.listAuditLogs({ page: 0, size: 1 }),
          auditLogService.listAuditLogs({ page: 0, size: 1, success: false }),
          auditLogService.listAuditLogs({ page: 0, size: 1, success: true }),
          auditLogService.listAuditLogs({ page: 0, size: 5 }),
          auditLogService.listAuditLogs({ page: 0, size: 50, from: since24hIso }),
        ]);

        if (
          !isSuccessResponse(allRes) ||
          !isSuccessResponse(failedRes) ||
          !isSuccessResponse(successRes) ||
          !isSuccessResponse(recentRes) ||
          !isSuccessResponse(recent24hRes)
        ) {
          toast.error("Failed to load dashboard metrics");
          return;
        }

        setMetrics({
          total: getPageMeta(allRes).totalElements,
          failed: getPageMeta(failedRes).totalElements,
          success: getPageMeta(successRes).totalElements,
        });
        setRecentLogs(Array.isArray(recentRes?.data?.content) ? recentRes.data.content : []);

        const snapshotRows = Array.isArray(recent24hRes?.data?.content) ? recent24hRes.data.content : [];
        setRecent24hLogs(snapshotRows);
        const uniqueActors = new Set(snapshotRows.map((row) => row.actorEmail || row.actorUserId || "unknown"));
        const latestRow = snapshotRows[0] || null;
        setActivitySnapshot({
          total24h: snapshotRows.length,
          failed24h: snapshotRows.filter((row) => row.success === false).length,
          uniqueActors24h: uniqueActors.size,
          latestActor: latestRow?.actorEmail || "--",
          latestAt: latestRow?.occurredAt || null,
        });
        setLastUpdatedAt(new Date().toISOString());
      } catch {
        toast.error("Failed to load dashboard metrics");
      } finally {
        setLoadingMetrics(false);
      }
    };

    loadMetrics();
  }, [canViewAuditMetrics]);

  const quickLinks = useMemo(() => {
    const links = [
      { to: "/attendance", label: "Attendance", icon: CheckCircle2 },
      { to: "/overtime", label: "Overtime", icon: Activity },
      { to: "/payroll", label: "Payroll", icon: Wallet },
      { to: "/profile", label: "My Profile", icon: UserCircle },
    ];
    if (canViewAuditMetrics) {
      links.unshift({ to: "/users", label: "Users", icon: Users2 });
      links.unshift({ to: "/departments", label: "Departments", icon: Building2 });
      links.push({ to: "/audit-logs", label: "Activity Logs", icon: Eye });
    }
    return links;
  }, [canViewAuditMetrics]);

  const openLogDetail = async (log) => {
    if (!log?.id) return;
    setDetailOpen(true);
    setDetailLoading(true);
    setSelectedLog(null);
    try {
      const res = await auditLogService.getAuditLogById(log.id);
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load audit log detail"));
        setDetailOpen(false);
        return;
      }
      setSelectedLog(res.data || null);
    } catch {
      toast.error("Failed to load audit log detail");
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  };

  const hourlyTrend = useMemo(() => {
    const buckets = Array.from({ length: 24 }, (_, hour) => ({ hour, total: 0, failed: 0 }));
    recent24hLogs.forEach((row) => {
      const occurredAt = new Date(row.occurredAt);
      if (Number.isNaN(occurredAt.getTime())) return;
      const hour = occurredAt.getHours();
      buckets[hour].total += 1;
      if (row.success === false) buckets[hour].failed += 1;
    });
    const maxTotal = Math.max(1, ...buckets.map((item) => item.total));
    return { buckets, maxTotal };
  }, [recent24hLogs]);

  const topActions = useMemo(() => {
    const counters = new Map();
    recent24hLogs.forEach((row) => {
      const label = getAuditActionLabel(row.httpMethod, row.endpointPattern);
      counters.set(label, (counters.get(label) || 0) + 1);
    });
    return Array.from(counters.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 6);
  }, [recent24hLogs]);

  const topActors = useMemo(() => {
    const counters = new Map();
    recent24hLogs.forEach((row) => {
      const actor = row.actorEmail || row.actorUserId || "Unknown";
      counters.set(actor, (counters.get(actor) || 0) + 1);
    });
    return Array.from(counters.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5);
  }, [recent24hLogs]);

  const activityPace = useMemo(() => {
    const total = activitySnapshot.total24h;
    const peak = hourlyTrend.buckets.reduce((best, current) => (current.total > best.total ? current : best), { hour: 0, total: 0 });
    return {
      avgPerHour: total ? (total / 24).toFixed(1) : "0.0",
      peakHour: `${String(peak.hour).padStart(2, "0")}:00`,
      peakCount: peak.total,
    };
  }, [activitySnapshot.total24h, hourlyTrend.buckets]);

  const successRate24h = useMemo(() => {
    if (!activitySnapshot.total24h) return 0;
    const successCount = activitySnapshot.total24h - activitySnapshot.failed24h;
    return Math.round((successCount / activitySnapshot.total24h) * 100);
  }, [activitySnapshot.failed24h, activitySnapshot.total24h]);

  const actionInbox = useMemo(() => {
    const items = [];

    if (systemMetrics.pendingOtItems > 0) {
      items.push({
        id: "pending-ot",
        title: "Overtime items awaiting action",
        detail: `${systemMetrics.pendingOtItems} request/report item(s) are still pending.`,
        to: "/overtime",
        severity: "warning",
      });
    }

    if (systemMetrics.payrollRowsThisMonth === 0 && (isAdminRole(user?.role) || isOfficeManagerRole(user?.role))) {
      items.push({
        id: "missing-payroll",
        title: "Payroll not generated for this month",
        detail: "No payroll rows found in current period.",
        to: "/payroll",
        severity: "danger",
      });
    }

    if (systemMetrics.attendanceOpen > 0) {
      items.push({
        id: "attendance-open-sessions",
        title: "Open attendance sessions detected",
        detail: `${systemMetrics.attendanceOpen} employee(s) have not checked out yet.`,
        to: "/attendance",
        severity: "warning",
      });
    }

    if (items.length === 0) {
      items.push({
        id: "all-good",
        title: "System operations look healthy",
        detail: "No urgent pending items in your current scope.",
        to: "/attendance",
        severity: "success",
      });
    }

    return items.slice(0, 4);
  }, [systemMetrics, user?.role]);

  const attendanceOfficeChart = useMemo(
    () => [
      { label: "Checked in", value: systemMetrics.attendanceCheckedIn, color: "#1677FF" },
      { label: "Checked out", value: systemMetrics.attendanceCheckedOut, color: "#2DBE8D" },
      { label: "Open sessions", value: systemMetrics.attendanceOpen, color: "#FA8C16" },
    ],
    [systemMetrics.attendanceCheckedIn, systemMetrics.attendanceCheckedOut, systemMetrics.attendanceOpen],
  );

  const overtimeOfficeChart = useMemo(
    () => [
      { label: "Pending requests", value: systemMetrics.otPendingRequests, color: "#FA8C16" },
      { label: "Pending reports", value: systemMetrics.otPendingReports, color: "#8B5CF6" },
      { label: "Approved reports", value: systemMetrics.otApprovedReports, color: "#2DBE8D" },
    ],
    [systemMetrics.otPendingRequests, systemMetrics.otPendingReports, systemMetrics.otApprovedReports],
  );

  const payrollOfficeChart = useMemo(
    () => [
      { label: "Draft", value: systemMetrics.payrollDraft, color: "#94A3B8" },
      { label: "Finalized", value: systemMetrics.payrollFinalized, color: "#1677FF" },
      { label: "Paid", value: systemMetrics.payrollPaid, color: "#2DBE8D" },
    ],
    [systemMetrics.payrollDraft, systemMetrics.payrollFinalized, systemMetrics.payrollPaid],
  );

  return (
    <div className="space-y-4">
      <div className="page-hero">
        <h1 className="page-title" style={{ fontSize: 26 }}>Dashboard</h1>
        <p className="page-subtitle">
          Welcome back 123456{user?.firstName ? `, ${user.firstName}` : ""}. System-wide snapshot across workforce, attendance, overtime, payroll and governance.
        </p>
        <div className="mt-2 inline-flex items-center gap-2 rounded-full px-3 py-1" style={{ backgroundColor: "rgba(22,119,255,0.10)" }}>
          <span style={{ width: 8, height: 8, borderRadius: 999, backgroundColor: "#1677FF" }} />
          <span style={{ fontSize: 12, color: "#1E40AF", fontWeight: 600 }}>
            Scope: {scopedOfficeId ? actorOfficeName : "All offices"}
          </span>
        </div>
        {canViewAuditMetrics ? (
          <div className="mt-2 inline-flex items-center gap-2 rounded-full px-3 py-1" style={{ backgroundColor: "rgba(45,190,141,0.12)" }}>
            <span style={{ width: 8, height: 8, borderRadius: 999, backgroundColor: "#2DBE8D" }} />
            <span style={{ fontSize: 12, color: "#2B6E52", fontWeight: 600 }}>
              {loadingMetrics ? "Refreshing dashboard..." : `Last updated ${formatDate(lastUpdatedAt)}`}
            </span>
          </div>
        ) : null}
      </div>

      <div className="glass-surface page-surface p-5 soft-ring">
        <div className="flex items-center justify-between gap-3 mb-4">
          <div>
            <h3 style={{ margin: 0, fontSize: 17, fontWeight: 700, color: "#0A0A0A" }}>System Snapshot</h3>
            <p style={{ margin: "4px 0 0", fontSize: 12, color: "#59607A" }}>
              Core operational metrics across modules ({scopedOfficeId ? "office scope" : "all offices"})
            </p>
          </div>
          <div className="flex items-center gap-2">
            {isAdminRole(user?.role) ? (
              <Select
                allowClear
                placeholder="All offices"
                value={selectedOfficeId}
                options={officeOptions}
                onChange={(value) => setSelectedOfficeId(value ?? undefined)}
                style={{ minWidth: 180 }}
              />
            ) : null}
            <span className="metric-chip" style={{ fontSize: 12 }}>Role: {formatRoleLabel(user?.role)}</span>
          </div>
        </div>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <MetricCard
            icon={Users2}
            label="Workforce"
            value={systemMetrics.totalUsers}
            helper="Total users in system"
            color="#1677FF"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={Building2}
            label="Organization"
            value={`${systemMetrics.totalDepartments} depts / ${systemMetrics.totalOffices} offices`}
            helper="Org structure coverage"
            color="#5B7CFF"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={Wallet}
            label="Payroll (This Month)"
            value={systemMetrics.payrollRowsThisMonth}
            helper="Generated payroll records"
            color="#2DBE8D"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={AlertTriangle}
            label="Pending OT"
            value={systemMetrics.pendingOtItems}
            helper="Pending requests + reports in current dashboard scope"
            color="#FA8C16"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={CheckCircle2}
            label="Attendance Today"
            value={`${systemMetrics.attendanceCheckedOut}/${systemMetrics.attendanceCheckedIn}`}
            helper="Checked-out / Checked-in employees"
            color="#13A8A8"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={AlertTriangle}
            label="Open Attendance Sessions"
            value={systemMetrics.attendanceOpen}
            helper="Checked-in without check-out"
            color="#FA8C16"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={Sparkles}
            label="Payroll Draft"
            value={systemMetrics.payrollDraft}
            helper="Draft payroll records this month"
            color="#94A3B8"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={Sparkles}
            label="Payroll Finalized"
            value={systemMetrics.payrollFinalized}
            helper="Finalized payroll records this month"
            color="#1677FF"
            loading={loadingSystemMetrics}
          />
          <MetricCard
            icon={Sparkles}
            label="Payroll Paid"
            value={systemMetrics.payrollPaid}
            helper="Paid payroll records this month"
            color="#2DBE8D"
            loading={loadingSystemMetrics}
          />
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-3">
        <MiniBarCard
          title="Attendance (Office Today)"
          subtitle="Check-in and check-out status"
          data={attendanceOfficeChart}
          loading={loadingSystemMetrics}
        />
        <MiniBarCard
          title="Overtime (Office Scope)"
          subtitle="Requests and reports progression"
          data={overtimeOfficeChart}
          loading={loadingSystemMetrics}
        />
        <MiniBarCard
          title="Payroll (Current Month)"
          subtitle="Payroll lifecycle by status"
          data={payrollOfficeChart}
          loading={loadingSystemMetrics}
        />
      </div>

      <div className="glass-surface page-surface p-5 soft-ring">
        <div className="flex items-center justify-between gap-3 mb-4">
          <h3 style={{ margin: 0, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>Action Inbox</h3>
          <span style={{ fontSize: 12, color: "#59607A" }}>Top priorities in your scope</span>
        </div>
        <div className="grid gap-3 md:grid-cols-2">
          {actionInbox.map((item) => (
            <div
              key={item.id}
              className="rounded-xl p-4 hover-lift"
              style={{
                border: "1px solid rgba(91,124,255,0.16)",
                background: "linear-gradient(135deg, rgba(255,255,255,0.9), rgba(245,248,255,0.82))",
              }}
            >
              <div className="flex items-center justify-between gap-2 mb-1">
                <strong style={{ fontSize: 14, color: "#0A0A0A" }}>{item.title}</strong>
                <Tag color={item.severity === "danger" ? "red" : item.severity === "warning" ? "orange" : item.severity === "success" ? "green" : "blue"} style={{ margin: 0 }}>
                  {item.severity}
                </Tag>
              </div>
              <p style={{ margin: 0, fontSize: 12, color: "#59607A" }}>{item.detail}</p>
              <div className="mt-3">
                <Link to={item.to} style={{ color: "#1677FF", fontSize: 13, fontWeight: 600 }}>
                  Open now
                </Link>
              </div>
            </div>
          ))}
        </div>
      </div>

      {canViewAuditMetrics ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard
          icon={Activity}
          label="Total Events"
          value={canViewAuditMetrics ? metrics.total : "--"}
          helper={canViewAuditMetrics ? "All tracked system and security actions" : "Visible for admin/office manager"}
          color="#1677FF"
          loading={loadingMetrics}
        />
        <MetricCard
          icon={AlertTriangle}
          label="Failed Events"
          value={canViewAuditMetrics ? metrics.failed : "--"}
          helper={canViewAuditMetrics ? "Events that did not complete successfully" : "Visible for admin/office manager"}
          color="#FA8C16"
          loading={loadingMetrics}
        />
        <MetricCard
          icon={CheckCircle2}
          label="Successful Events"
          value={canViewAuditMetrics ? metrics.success : "--"}
          helper={canViewAuditMetrics ? "Successful scoped operations" : "Visible for admin/office manager"}
          color="#2DBE8D"
          loading={loadingMetrics}
        />
        <MetricCard
          icon={Sparkles}
          label="24h Success Rate"
          value={canViewAuditMetrics ? `${successRate24h}%` : "--"}
          helper={canViewAuditMetrics ? "Share of successful events in the last 24h" : "Visible for admin/office manager"}
          color="#8B5CF6"
          loading={loadingMetrics}
        />
        </div>
      ) : null}

      {canViewAuditMetrics ? (
        <>
          <div className="flex items-center justify-between">
            <h3 style={{ margin: 0, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>Compliance and Activity Overview</h3>
            <Link to="/audit-logs" style={{ color: "#1677FF", fontSize: 13, fontWeight: 600 }}>Open full audit logs</Link>
          </div>
          <div className="grid gap-4 xl:grid-cols-3">
            <div className="glass-surface page-surface p-5 soft-ring xl:col-span-2">
              <div className="flex items-center justify-between gap-3 mb-4">
                <div className="flex items-center gap-2">
                  <BarChart3 className="w-5 h-5" style={{ color: "#1677FF" }} />
                  <div>
                    <h3 style={{ margin: 0, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>24h Activity Trend</h3>
                    <p style={{ margin: "4px 0 0", fontSize: 12, color: "#59607A" }}>Event volume by hour</p>
                  </div>
                </div>
                <Link to="/audit-logs" style={{ color: "#1677FF", fontSize: 13, fontWeight: 600 }}>
                  Open logs
                </Link>
              </div>
              <HourlyBars buckets={hourlyTrend.buckets} maxTotal={hourlyTrend.maxTotal} loading={loadingMetrics} />
            </div>

            <div className="glass-surface page-surface p-5 soft-ring">
              <div className="flex items-center gap-2 mb-3">
                <PieChart className="w-5 h-5" style={{ color: "#8B5CF6" }} />
                <h3 style={{ margin: 0, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>24h Health</h3>
              </div>
              <SuccessRing value={successRate24h} loading={loadingMetrics} />
              <div className="space-y-2 mt-4">
                <StatLine label="Events" value={loadingMetrics ? "..." : activitySnapshot.total24h} />
                <StatLine label="Failed" value={loadingMetrics ? "..." : activitySnapshot.failed24h} />
                <StatLine label="Unique actors" value={loadingMetrics ? "..." : activitySnapshot.uniqueActors24h} />
                <StatLine
                  label="Latest actor"
                  value={loadingMetrics ? "..." : activitySnapshot.latestActor}
                  helper={activitySnapshot.latestAt ? formatDate(activitySnapshot.latestAt) : "No activity"}
                />
              </div>
            </div>
          </div>

          <div className="grid gap-4 xl:grid-cols-3">
            <div className="glass-surface page-surface p-5 soft-ring">
              <h3 style={{ margin: 0, marginBottom: 12, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>Top Actions (24h)</h3>
              {topActions.length === 0 ? (
                <p style={{ color: "#8C8C8C", fontSize: 13, margin: 0 }}>No actions recorded in the last 24h.</p>
              ) : (
                <div className="space-y-3">
                  {topActions.map(([label, count], index) => {
                    const max = topActions[0]?.[1] || 1;
                    const width = Math.max(8, Math.round((count / max) * 100));
                    return (
                      <div key={label}>
                        <div className="flex items-center justify-between" style={{ marginBottom: 6 }}>
                          <span style={{ fontSize: 12, color: "#0A0A0A", fontWeight: index < 3 ? 700 : 500 }}>{label}</span>
                          <span style={{ fontSize: 12, color: "#59607A" }}>{count}</span>
                        </div>
                        <div style={{ height: 8, borderRadius: 999, background: "rgba(91,124,255,0.14)" }}>
                          <div
                            style={{
                              width: `${width}%`,
                              height: "100%",
                              borderRadius: 999,
                              background: "linear-gradient(90deg, #5B7CFF, #2DBE8D)",
                            }}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}

              <div style={{ height: 1, background: "rgba(22,119,255,0.12)", margin: "14px 0" }} />
              <h4 style={{ margin: 0, marginBottom: 10, fontSize: 13, fontWeight: 700, color: "#0A0A0A" }}>Top Actors</h4>
              <div className="space-y-2">
                {topActors.length === 0 ? (
                  <p style={{ color: "#8C8C8C", fontSize: 12, margin: 0 }}>No actor activity yet.</p>
                ) : (
                  topActors.map(([actor, count]) => (
                    <ActorPill key={actor} actor={actor} count={count} />
                  ))
                )}
              </div>

              <div style={{ height: 1, background: "rgba(22,119,255,0.12)", margin: "14px 0" }} />
              <h4 style={{ margin: 0, marginBottom: 10, fontSize: 13, fontWeight: 700, color: "#0A0A0A" }}>Activity Pace</h4>
              <div className="space-y-2">
                <StatLine label="Average events/hour" value={loadingMetrics ? "..." : activityPace.avgPerHour} />
                <StatLine label="Peak hour" value={loadingMetrics ? "..." : activityPace.peakHour} helper={`Peak volume: ${activityPace.peakCount}`} />
              </div>
            </div>

            <div className="glass-surface page-surface p-5 soft-ring xl:col-span-2">
              <h3 style={{ margin: 0, marginBottom: 12, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>Recent System Activity</h3>
              <div className="grid gap-3">
                {recentLogs.length === 0 ? (
                  <div
                    className="rounded-xl p-4"
                    style={{ border: "1px dashed rgba(91,124,255,0.2)", color: "#8C8C8C", fontSize: 13 }}
                  >
                    No recent activity.
                  </div>
                ) : (
                  recentLogs.map((log) => (
                    <div
                      key={log.id}
                      className="rounded-xl p-4 hover-lift"
                      style={{
                        border: "1px solid rgba(91,124,255,0.12)",
                        background: "linear-gradient(135deg, rgba(255,255,255,0.9), rgba(245,248,255,0.82))",
                      }}
                    >
                      <div className="flex flex-wrap items-center justify-between gap-3">
                        <div className="min-w-0">
                          <div className="flex flex-wrap items-center gap-2 mb-1">
                            <span className="metric-chip" style={{ padding: "4px 10px", fontSize: 12 }}>
                              {getAuditStatusLabel(log.success)}
                            </span>
                            <span style={{ fontSize: 13, fontWeight: 700, color: "#0A0A0A" }}>
                              {getAuditActionLabel(log.httpMethod, log.endpointPattern)}
                            </span>
                          </div>
                          <div style={{ fontSize: 12, color: "#59607A" }}>
                            {log.actorEmail || "Unknown"} · {formatDate(log.occurredAt)}
                          </div>
                        </div>
                      </div>
                      <div className="mt-3 flex justify-end">
                        <Button size="small" icon={<Eye className="w-4 h-4" />} onClick={() => openLogDetail(log)}>
                          View details
                        </Button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </>
      ) : null}
      <div className="glass-surface page-surface p-5 soft-ring">
        <h3 style={{ margin: 0, marginBottom: 12, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>Quick Actions</h3>
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {quickLinks.map((item) => {
            const Icon = item.icon;
            return (
              <Link
                key={item.to}
                to={item.to}
                className="rounded-xl p-4 hover-lift"
                style={{
                  border: "1px solid rgba(91,124,255,0.16)",
                  background: "linear-gradient(135deg, rgba(255,255,255,0.86), rgba(245,248,255,0.78))",
                }}
              >
                <div className="flex items-center gap-3">
                  <div
                    className="w-9 h-9 rounded-lg flex items-center justify-center"
                    style={{ backgroundColor: "rgba(91,124,255,0.14)", color: "#5B7CFF" }}
                  >
                    <Icon className="w-5 h-5" />
                  </div>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 700, color: "#0A0A0A" }}>{item.label}</div>
                    <div style={{ fontSize: 12, color: "#59607A" }}>Open feature</div>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      </div>

      <Drawer
        title="Audit Log Details"
        open={detailOpen}
        width={520}
        onClose={() => {
          setDetailOpen(false);
          setSelectedLog(null);
        }}
      >
        {detailLoading ? (
          <p style={{ color: "#8C8C8C" }}>Loading...</p>
        ) : !selectedLog ? (
          <p style={{ color: "#8C8C8C" }}>No data available</p>
        ) : (
          <div className="space-y-2">
            <Field label="Time" value={formatDate(selectedLog.occurredAt)} />
            <Field label="Actor" value={selectedLog.actorEmail || "--"} />
            <Field label="Action" value={getAuditActionLabel(selectedLog.httpMethod, selectedLog.endpointPattern)} />
            <Field label="Status" value={<Tag color={getAuditStatusColor(selectedLog.success)} style={{ margin: 0 }}>{getAuditStatusLabel(selectedLog.success)}</Tag>} />
          </div>
        )}
      </Drawer>
    </div>
  );
}

function MetricCard({ icon, label, value, helper, color, loading = false }) {
  const IconComponent = icon;
  const displayValue = loading ? "..." : value;
  const isLongValue = String(displayValue || "").length > 16;
  return (
    <div className="glass-surface page-surface p-4 soft-ring min-h-[152px]">
      <div className="flex items-center gap-3">
        <div
          className="w-9 h-9 rounded-lg flex items-center justify-center"
          style={{ backgroundColor: `${color}22`, color }}
        >
          <IconComponent className="w-5 h-5" />
        </div>
        <span style={{ fontSize: 12, color: "#6B7280", fontWeight: 600 }}>{label}</span>
      </div>

      <div
        style={{
          marginTop: 12,
          fontSize: isLongValue ? 18 : 30,
          fontWeight: 750,
          color: "#0A0A0A",
          lineHeight: 1.2,
          letterSpacing: "-0.01em",
          wordBreak: "break-word",
        }}
      >
        {displayValue}
      </div>
      <div style={{ marginTop: 8, fontSize: 12, color: "#59607A", lineHeight: 1.45 }}>{helper}</div>
    </div>
  );
}

function MiniBarCard({ title, subtitle, data, loading = false }) {
  const safeData = Array.isArray(data) ? data : [];
  const maxValue = Math.max(1, ...safeData.map((item) => Number(item?.value || 0)));

  return (
    <div className="glass-surface page-surface p-5 soft-ring">
      <h3 style={{ margin: 0, fontSize: 15, fontWeight: 700, color: "#0A0A0A" }}>{title}</h3>
      <p style={{ margin: "4px 0 12px", fontSize: 12, color: "#59607A" }}>{subtitle}</p>

      {loading ? (
        <p style={{ margin: 0, fontSize: 12, color: "#8C8C8C" }}>Loading chart...</p>
      ) : (
        <div className="space-y-3">
          {safeData.map((item) => {
            const value = Number(item?.value || 0);
            const width = Math.max(10, Math.round((value / maxValue) * 100));
            return (
              <div key={item.label}>
                <div className="flex items-center justify-between" style={{ marginBottom: 6 }}>
                  <span style={{ fontSize: 12, color: "#0A0A0A", fontWeight: 600 }}>{item.label}</span>
                  <span style={{ fontSize: 12, color: "#59607A", fontWeight: 700 }}>{value}</span>
                </div>
                <div style={{ height: 8, borderRadius: 999, background: "rgba(91,124,255,0.12)" }}>
                  <div style={{ width: `${width}%`, height: "100%", borderRadius: 999, background: item.color || "#5B7CFF" }} />
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}


function formatDate(value) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "--";
  return date.toLocaleString();
}

function HourlyBars({ buckets, maxTotal, loading }) {
  return (
    <div>
      <div style={{ height: 180, display: "grid", gridTemplateColumns: "repeat(24, minmax(0, 1fr))", gap: 4, alignItems: "end" }}>
        {buckets.map((item) => {
          const height = loading ? 12 : Math.max(6, Math.round((item.total / maxTotal) * 100));
          const failedHeight = item.total ? Math.max(2, Math.round((item.failed / item.total) * height)) : 0;
          return (
            <div key={item.hour} title={`${item.hour}:00 - total ${item.total}, failed ${item.failed}`} style={{ position: "relative", height: "100%" }}>
              <div
                style={{
                  position: "absolute",
                  bottom: 0,
                  width: "100%",
                  height: `${height}%`,
                  borderRadius: 6,
                  background: "rgba(91,124,255,0.26)",
                }}
              />
              {failedHeight > 0 ? (
                <div
                  style={{
                    position: "absolute",
                    bottom: 0,
                    width: "100%",
                    height: `${failedHeight}%`,
                    borderRadius: 6,
                    background: "rgba(250,140,22,0.85)",
                  }}
                />
              ) : null}
            </div>
          );
        })}
      </div>
      <div className="flex items-center justify-between" style={{ marginTop: 10, fontSize: 11, color: "#8C8C8C" }}>
        <span>00:00</span>
        <span>12:00</span>
        <span>23:00</span>
      </div>
      <div className="flex items-center gap-4" style={{ marginTop: 8, fontSize: 12, color: "#59607A" }}>
        <LegendDot color="rgba(91,124,255,0.8)" label="Total" />
        <LegendDot color="rgba(250,140,22,0.85)" label="Failed" />
      </div>
    </div>
  );
}

function SuccessRing({ value, loading }) {
  const safeValue = Math.max(0, Math.min(100, Number(value) || 0));
  const background = `conic-gradient(#2DBE8D ${safeValue * 3.6}deg, rgba(22,119,255,0.12) 0deg)`;
  return (
    <div className="flex justify-center">
      <div
        style={{
          width: 148,
          height: 148,
          borderRadius: "50%",
          background,
          display: "grid",
          placeItems: "center",
          boxShadow: "inset 0 0 0 1px rgba(45,190,141,0.18)",
        }}
      >
        <div
          style={{
            width: 112,
            height: 112,
            borderRadius: "50%",
            background: "#fff",
            display: "grid",
            placeItems: "center",
            textAlign: "center",
          }}
        >
          <div>
            <div style={{ fontSize: 24, fontWeight: 800, color: "#0A0A0A", lineHeight: 1.1 }}>{loading ? "..." : `${safeValue}%`}</div>
            <div style={{ fontSize: 12, color: "#8C8C8C" }}>Success</div>
          </div>
        </div>
      </div>
    </div>
  );
}

function StatLine({ label, value, helper }) {
  return (
    <div className="rounded-lg p-2" style={{ border: "1px solid rgba(22,119,255,0.12)", backgroundColor: "rgba(255,255,255,0.75)" }}>
      <div className="flex items-center justify-between gap-3">
        <span style={{ fontSize: 12, color: "#59607A" }}>{label}</span>
        <span style={{ fontSize: 12, color: "#0A0A0A", fontWeight: 700 }}>{value}</span>
      </div>
      {helper ? <div style={{ fontSize: 11, color: "#8C8C8C", marginTop: 2 }}>{helper}</div> : null}
    </div>
  );
}

function LegendDot({ color, label }) {
  return (
    <span className="flex items-center gap-2">
      <span style={{ width: 10, height: 10, borderRadius: 999, backgroundColor: color }} />
      {label}
    </span>
  );
}

function ActorPill({ actor, count }) {
  const initial = String(actor || "U").trim().charAt(0).toUpperCase() || "U";
  return (
    <div className="flex items-center justify-between gap-3 rounded-lg px-2 py-2" style={{ backgroundColor: "rgba(91,124,255,0.08)" }}>
      <div className="flex items-center gap-2 min-w-0">
        <span
          className="w-6 h-6 rounded-full flex items-center justify-center"
          style={{ fontSize: 11, fontWeight: 700, color: "#5B7CFF", backgroundColor: "rgba(91,124,255,0.18)" }}
        >
          {initial}
        </span>
        <span style={{ fontSize: 12, color: "#0A0A0A", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{actor}</span>
      </div>
      <span style={{ fontSize: 12, fontWeight: 700, color: "#59607A" }}>{count}</span>
    </div>
  );
}

function Field({ label, value, mono = false }) {
  return (
    <div className="rounded-lg p-3" style={{ border: "1px solid #E8E8E8", backgroundColor: "#fff" }}>
      <div style={{ fontSize: 12, color: "#8C8C8C", marginBottom: 4 }}>{label}</div>
      <div
        style={{
          fontSize: 13,
          color: "#0A0A0A",
          fontFamily: mono ? "JetBrains Mono, monospace" : "inherit",
          wordBreak: "break-word",
        }}
      >
        {value}
      </div>
    </div>
  );
}



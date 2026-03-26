import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Button, Form, Input, InputNumber, Modal, Select, Table, Tag } from "antd";
import { toast } from "sonner";
import attendanceService from "../api/attendanceService";
import { isSuccessResponse, getResponseMessage } from "@/utils/apiResponse";
import { AttendanceHeader } from "../components/AttendanceHeader";
import { AttendanceCheckPanel } from "../components/AttendanceCheckPanel";
import { AttendanceHistoryTable } from "../components/AttendanceHistoryTable";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isEmployeeRole, isDepartmentManagerRole, isManagerRole, isOfficeManagerRole } from "@/utils/role";

export function AttendancePage() {
  const { user } = useAuth();
  const canApprove = isManagerRole(user?.role);
  const isDeptManager = isDepartmentManagerRole(user?.role);
  const isOfficeManager = isOfficeManagerRole(user?.role);
  const canCreateOt =
    isEmployeeRole(user?.role) ||
    isDepartmentManagerRole(user?.role) ||
    isOfficeManagerRole(user?.role);
  const canViewDepartmentAttendance = isDepartmentManagerRole(user?.role);
  const [currentTime, setCurrentTime] = useState(new Date());
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [todayLog, setTodayLog] = useState(null);
  const [departmentTodayLogs, setDepartmentTodayLogs] = useState([]);
  const [departmentTodayLoading, setDepartmentTodayLoading] = useState(false);
  const [otRequestOpen, setOtRequestOpen] = useState(false);
  const [otReportOpen, setOtReportOpen] = useState(false);
  const [otRequests, setOtRequests] = useState([]);
  const [otReports, setOtReports] = useState([]);
  const [pendingOtRequests, setPendingOtRequests] = useState([]);
  const [pendingOtReports, setPendingOtReports] = useState([]);
  const [otLoading, setOtLoading] = useState(false);
  const [submittingOt, setSubmittingOt] = useState(false);
  const [myReqStatusFilter, setMyReqStatusFilter] = useState("ALL");
  const [myRepStatusFilter, setMyRepStatusFilter] = useState("ALL");
  const [pendingReqStatusFilter, setPendingReqStatusFilter] = useState("ALL");
  const [pendingRepStatusFilter, setPendingRepStatusFilter] = useState("ALL");
  const [requestForm] = Form.useForm();
  const [reportForm] = Form.useForm();

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const timeString = useMemo(
    () =>
      currentTime.toLocaleTimeString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false,
      }),
    [currentTime],
  );

  const dateString = useMemo(
    () =>
      currentTime.toLocaleDateString("en-US", {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric",
      }),
    [currentTime],
  );

  const checkedIn = Boolean(todayLog?.checkInTime) && !todayLog?.checkOutTime;
  const checkInTimeText = todayLog?.checkInTime
    ? String(todayLog.checkInTime).replace("T", " ").slice(11, 16)
    : null;

  const loadToday = async () => {
    setLoading(true);
    try {
      const res = await attendanceService.getToday();
      if (!isSuccessResponse(res)) {
        throw new Error(getResponseMessage(res, "Failed to load attendance"));
      }
      setTodayLog(res?.data ?? null);
    } catch {
      setTodayLog(null);
      toast.error("Failed to load attendance");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadToday();
  }, []);

  const loadDepartmentToday = useCallbaack(async () => {
    setDepartmentTodayLoading(true);
    try {
      const res = await attendanceService.getDepartmentToday();
      if (!isSuccessResponse(res)) {
        throw new Error(getResponseMessage(res, "Failed to load department attendance"));
      }
      setDepartmentTodayLogs(Array.isArray(res?.data) ? res.data : []);
    } catch {
      toast.error("Failed to load department attendance");
      setDepartmentTodayLogs([]);
    } finally {
      setDepartmentTodayLoading(false);
    }
  }, []);

  useEffect(() => {
    if (canViewDepartmentAttendance) {
      loadDepartmentToday().catch(() => {});
    }
  }, [canViewDepartmentAttendance, loadDepartmentToday]);

  const loadOtData = useCallback(async () => {
    setOtLoading(true);
    try {
      const [myReqRes, myRepRes, pendingReqRes, pendingRepRes] = await Promise.all([
        attendanceService.getMyOtRequests(),
        attendanceService.getMyOtReports(),
        canApprove ? attendanceService.getPendingOtRequests() : Promise.resolve({ data: [] }),
        canApprove ? attendanceService.getPendingOtReports() : Promise.resolve({ data: [] }),
      ]);

      setOtRequests(isSuccessResponse(myReqRes) && Array.isArray(myReqRes?.data) ? myReqRes.data : []);
      setOtReports(isSuccessResponse(myRepRes) && Array.isArray(myRepRes?.data) ? myRepRes.data : []);
      setPendingOtRequests(
        canApprove && isSuccessResponse(pendingReqRes) && Array.isArray(pendingReqRes?.data)
          ? pendingReqRes.data
          : [],
      );
      setPendingOtReports(
        canApprove && isSuccessResponse(pendingRepRes) && Array.isArray(pendingRepRes?.data)
          ? pendingRepRes.data
          : [],
      );
    } catch {
      toast.error("Failed to load OT data");
    } finally {
      setOtLoading(false);
    }
  }, [canApprove]);

  useEffect(() => {
    loadOtData();
  }, [loadOtData]);

  const handleCheckIn = async () => {
    if (actionLoading) return;
    setActionLoading(true);
    try {
      const res = await attendanceService.checkIn();
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Check-in failed"));
      }
      toast.success(getResponseMessage(res, "Checked in successfully"));
      await loadToday();
    } catch {
      toast.error("Check-in failed");
    } finally {
      setActionLoading(false);
    }
  };

  const handleCheckOut = async () => {
    if (actionLoading) return;
    setActionLoading(true);
    try {
      const res = await attendanceService.checkOut();
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Check-out failed"));
      }
      toast.success(getResponseMessage(res, "Checked out successfully"));
      await loadToday();
    } catch {
      toast.error("Check-out failed");
    } finally {
      setActionLoading(false);
    }
  };

  const historyRecords = todayLog ? [todayLog] : [];
  const filteredMyOtRequests = useMemo(
    () => otRequests.filter((r) => myReqStatusFilter === "ALL" || r.status === myReqStatusFilter),
    [otRequests, myReqStatusFilter],
  );
  const filteredMyOtReports = useMemo(
    () => otReports.filter((r) => myRepStatusFilter === "ALL" || r.status === myRepStatusFilter),
    [otReports, myRepStatusFilter],
  );
  const filteredPendingReq = useMemo(
    () => pendingOtRequests.filter((r) => pendingReqStatusFilter === "ALL" || r.status === pendingReqStatusFilter),
    [pendingOtRequests, pendingReqStatusFilter],
  );
  const filteredPendingRep = useMemo(
    () => pendingOtReports.filter((r) => pendingRepStatusFilter === "ALL" || r.status === pendingRepStatusFilter),
    [pendingOtReports, pendingRepStatusFilter],
  );

  const hasApprovedOtRequestForToday = useMemo(() => {
    if (!todayLog?.logDate) return false;
    return otRequests.some(
      (r) => r?.status === "APPROVED" && String(r?.logDate) === String(todayLog?.logDate),
    );
  }, [otRequests, todayLog?.logDate]);

  const otStatusTag = (status) => {
    if (status === "APPROVED") return <Tag color="success">APPROVED</Tag>;
    if (status === "REJECTED") return <Tag color="error">REJECTED</Tag>;
    if (status === "CANCELLED") return <Tag color="default">CANCELLED</Tag>;
    if (status === "PENDING_DEPARTMENT") return <Tag color="warning">Pending (Dept)</Tag>;
    if (status === "PENDING_OFFICE") return <Tag color="warning">Pending (Office)</Tag>;
    return <Tag color="warning">Pending</Tag>;
  };

  const handleCreateOtRequest = async () => {
    try {
      const values = await requestForm.validateFields();
      setSubmittingOt(true);
      const res = await attendanceService.createOtRequest(values);
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Create OT request failed"));
      }
      toast.success(getResponseMessage(res, "OT request created successfully"));
      setOtRequestOpen(false);
      requestForm.resetFields();
      await loadOtData();
    } catch (e) {
      if (e?.errorFields) return;
      toast.error("Create OT request failed");
    } finally {
      setSubmittingOt(false);
    }
  };

  const handleCreateOtReport = async () => {
    if (!todayLog?.id) {
      toast.error("No attendance record to submit OT report");
      return;
    }
    try {
      const values = await reportForm.validateFields();
      setSubmittingOt(true);
      const res = await attendanceService.createOtReport({
        attendanceLogId: todayLog.id,
        reportedOtHours: values.reportedOtHours,
        reportNote: values.reportNote,
      });
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Create OT report failed"));
      }
      toast.success(getResponseMessage(res, "OT report submitted successfully"));
      setOtReportOpen(false);
      reportForm.resetFields();
      await loadOtData();
    } catch (e) {
      if (e?.errorFields) return;
      toast.error("Create OT report failed");
    } finally {
      setSubmittingOt(false);
    }
  };

  const handleDecision = async (type, id, approved) => {
    try {
      const serviceCall =
        type === "request" ? attendanceService.decideOtRequest : attendanceService.decideOtReport;
      const res = await serviceCall(id, { approved, decisionNote: approved ? "Approved" : "Rejected" });
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Decision failed"));
      }
      toast.success(getResponseMessage(res, "Decision updated"));
      await loadOtData();
    } catch {
      toast.error("Decision failed");
    }
  };

  return (
    <div className="space-y-6">
      <AttendanceHeader />

      {loading ? (
        <div className="rounded-xl p-8" style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
          <p style={{ color: "#8C8C8C", fontSize: "14px" }}>Loading attendance...</p>
        </div>
      ) : (
        <div className="space-y-3">
          <AttendanceCheckPanel
            currentTimeText={timeString}
            currentDateText={dateString}
            checkedIn={checkedIn}
            checkInTimeText={checkInTimeText}
            actionLoading={actionLoading}
            onCheckIn={handleCheckIn}
            onCheckOut={handleCheckOut}
          />
          <Alert
            type="info"
            showIcon
            message={
              todayLog?.checkOutTime
                ? `Today: ${todayLog?.workedHours ?? 0}h regular, ${todayLog?.otHours ?? 0}h OT`
                : "Tip: Payroll is calculated by actual working hours."
            }
          />
        </div>
      )}

      <AttendanceHistoryTable records={historyRecords} />

      {canViewDepartmentAttendance && (
        <div className="mt-6">
          {departmentTodayLoading ? (
            <div
              className="rounded-xl p-8"
              style={{
                backgroundColor: "#FFFFFF",
                boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
              }}
            >
              <p style={{ color: "#8C8C8C", fontSize: "14px" }}>
                Loading department attendance...
              </p>
            </div>
          ) : (
            <AttendanceHistoryTable
              records={departmentTodayLogs}
              title="Department Attendance (Today)"
            />
          )}
        </div>
      )}

      <div className="rounded-xl p-4 space-y-3" style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
        <div className="flex items-center justify-between">
          <h3 style={{ fontSize: 16, fontWeight: 600, color: "#0A0A0A" }}>My OT Workflow</h3>
          <div className="flex gap-2">
            {canCreateOt && (
              <Button onClick={() => setOtRequestOpen(true)}>New OT Request</Button>
            )}
            {canCreateOt && (
              <Button
                type="primary"
                onClick={() => setOtReportOpen(true)}
                disabled={!todayLog?.checkOutTime || !hasApprovedOtRequestForToday}
              >
                Submit OT Report
              </Button>
            )}
          </div>
        </div>
        <Table
          rowKey={(r) => r.id}
          loading={otLoading}
          dataSource={filteredMyOtRequests}
          size="small"
          pagination={{ pageSize: 5 }}
          columns={[
            { title: "Date", dataIndex: "logDate", key: "logDate" },
            { title: "Reason", dataIndex: "reason", key: "reason", ellipsis: true },
            { title: "Status", key: "status", render: (_, r) => otStatusTag(r.status) },
          ]}
          locale={{ emptyText: "No OT requests yet." }}
        />
        <Select
          value={myReqStatusFilter}
          onChange={setMyReqStatusFilter}
          style={{ width: 180, marginBottom: 16 }}
          options={[
            { value: "ALL", label: "All request status" },
            { value: "PENDING_DEPARTMENT", label: "Pending (Dept)" },
            { value: "PENDING_OFFICE", label: "Pending (Office)" },
            { value: "PENDING", label: "Pending (Legacy)" },
            { value: "APPROVED", label: "Approved" },
            { value: "REJECTED", label: "Rejected" },
            { value: "CANCELLED", label: "Cancelled" },
          ]}
        />
        <Table
          rowKey={(r) => r.id}
          loading={otLoading}
          dataSource={filteredMyOtReports}
          size="small"
          pagination={{ pageSize: 5 }}
          columns={[
            { title: "OT Hours", dataIndex: "reportedOtHours", key: "reportedOtHours", render: (v) => `${v ?? 0}h` },
            { title: "Report Note", dataIndex: "reportNote", key: "reportNote", ellipsis: true },
            { title: "Status", key: "status", render: (_, r) => otStatusTag(r.status) },
          ]}
          locale={{ emptyText: "No OT reports yet." }}
        />
        <Select
          value={myRepStatusFilter}
          onChange={setMyRepStatusFilter}
          style={{ width: 180 }}
          options={[
            { value: "ALL", label: "All report status" },
            { value: "PENDING_DEPARTMENT", label: "Pending (Dept)" },
            { value: "PENDING_OFFICE", label: "Pending (Office)" },
            { value: "PENDING", label: "Pending (Legacy)" },
            { value: "APPROVED", label: "Approved" },
            { value: "REJECTED", label: "Rejected" },
          ]}
        />
      </div>

      {canApprove && (
        <div className="rounded-xl p-4 space-y-3" style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
          <h3 style={{ fontSize: 16, fontWeight: 600, color: "#0A0A0A" }}>Manager Approval Queue</h3>
          <Table
            rowKey={(r) => r.id}
            loading={otLoading}
            dataSource={filteredPendingReq}
            size="small"
            pagination={{ pageSize: 5 }}
            columns={[
              { title: "User", dataIndex: "userName", key: "userName" },
              { title: "Date", dataIndex: "logDate", key: "logDate" },
              { title: "Reason", dataIndex: "reason", key: "reason", ellipsis: true },
              {
                title: "Action",
                key: "action",
                render: (_, r) => (
                  (() => {
                    const canDecideRequest =
                      (isDeptManager && (r.status === "PENDING_DEPARTMENT" || r.status === "PENDING")) ||
                      (isOfficeManager && r.status === "PENDING_OFFICE");

                    if (!canDecideRequest) return <span style={{ color: "#8C8C8C" }}>—</span>;

                    return (
                      <div className="flex gap-2">
                        <Button size="small" type="primary" onClick={() => handleDecision("request", r.id, true)}>
                          Approve
                        </Button>
                        <Button size="small" danger onClick={() => handleDecision("request", r.id, false)}>
                          Reject
                        </Button>
                      </div>
                    );
                  })()
                ),
              },
            ]}
            locale={{ emptyText: "No pending OT requests." }}
          />
          <Select
            value={pendingReqStatusFilter}
            onChange={setPendingReqStatusFilter}
            style={{ width: 180, marginBottom: 16 }}
            options={[
              { value: "ALL", label: "All request status" },
              { value: "PENDING_DEPARTMENT", label: "Pending (Dept)" },
              { value: "PENDING_OFFICE", label: "Pending (Office)" },
              { value: "PENDING", label: "Pending (Legacy)" },
              { value: "APPROVED", label: "Approved" },
              { value: "REJECTED", label: "Rejected" },
              { value: "CANCELLED", label: "Cancelled" },
            ]}
          />
          <Table
            rowKey={(r) => r.id}
            loading={otLoading}
            dataSource={filteredPendingRep}
            size="small"
            pagination={{ pageSize: 5 }}
            columns={[
              { title: "OT Hours", dataIndex: "reportedOtHours", key: "reportedOtHours", render: (v) => `${v ?? 0}h` },
              { title: "Note", dataIndex: "reportNote", key: "reportNote", ellipsis: true },
              {
                title: "Action",
                key: "action",
                render: (_, r) => (
                  (() => {
                    const canDecideReport =
                      (isDeptManager && (r.status === "PENDING_DEPARTMENT" || r.status === "PENDING")) ||
                      (isOfficeManager && r.status === "PENDING_OFFICE");

                    if (!canDecideReport) return <span style={{ color: "#8C8C8C" }}>—</span>;

                    return (
                      <div className="flex gap-2">
                        <Button size="small" type="primary" onClick={() => handleDecision("report", r.id, true)}>
                          Approve
                        </Button>
                        <Button size="small" danger onClick={() => handleDecision("report", r.id, false)}>
                          Reject
                        </Button>
                      </div>
                    );
                  })()
                ),
              },
            ]}
            locale={{ emptyText: "No pending OT reports." }}
          />
          <Select
            value={pendingRepStatusFilter}
            onChange={setPendingRepStatusFilter}
            style={{ width: 180 }}
            options={[
              { value: "ALL", label: "All report status" },
              { value: "PENDING_DEPARTMENT", label: "Pending (Dept)" },
              { value: "PENDING_OFFICE", label: "Pending (Office)" },
              { value: "PENDING", label: "Pending (Legacy)" },
              { value: "APPROVED", label: "Approved" },
              { value: "REJECTED", label: "Rejected" },
            ]}
          />
        </div>
      )}

      <Modal
        title="Create OT Request"
        open={otRequestOpen}
        onCancel={() => setOtRequestOpen(false)}
        onOk={handleCreateOtRequest}
        okButtonProps={{ loading: submittingOt }}
      >
        <Form form={requestForm} layout="vertical">
          <Form.Item
            name="logDate"
            label="OT Date"
            rules={[{ required: true, message: "OT date is required" }]}
            initialValue={new Date().toISOString().slice(0, 10)}
          >
            <Input type="date" />
          </Form.Item>
          <Form.Item
            name="reason"
            label="Reason"
            rules={[{ required: true, message: "Reason is required" }]}
          >
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Submit OT Report"
        open={otReportOpen}
        onCancel={() => setOtReportOpen(false)}
        onOk={handleCreateOtReport}
        okButtonProps={{ loading: submittingOt }}
      >
        <Form form={reportForm} layout="vertical">
          <Form.Item
            name="reportedOtHours"
            label="Reported OT Hours"
            rules={[{ required: true, message: "Reported OT hours is required" }]}
          >
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="reportNote"
            label="Report Note"
              rules={[{ required: true, message: "Report note (evidence) is required" }]}
          >
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

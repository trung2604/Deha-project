import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, Form, Input, Modal } from "antd";
import { CheckCircle2, ClipboardList, Clock3, FileCheck2 } from "lucide-react";
import { toast } from "sonner";
import overtimeService from "../api/overtimeService";
import attendanceService from "@/features/attendance/api/attendanceService";
import {
  getOptimisticConflictMessage,
  getResponseMessage,
  isOptimisticConflictResponse,
  isSuccessResponse,
} from "@/utils/apiResponse";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole, isEmployeeRole, isDepartmentManagerRole, isManagerRole, isOfficeManagerRole } from "@/utils/role";
import { OvertimeSection } from "../components/OvertimeSection";
import { isPendingStatus, OT_FILTER_ALL, OT_STATUS } from "../constants/overtimeStatus.constants";

function parseApiDateTime(dateTimeText) {
  if (!dateTimeText) return null;
  const parsed = new Date(dateTimeText);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

function formatDurationFromSeconds(totalSeconds) {
  const safeSeconds = Math.max(0, Number(totalSeconds) || 0);
  const hours = String(Math.floor(safeSeconds / 3600)).padStart(2, "0");
  const minutes = String(Math.floor((safeSeconds % 3600) / 60)).padStart(2, "0");
  const seconds = String(safeSeconds % 60).padStart(2, "0");
  return `${hours}:${minutes}:${seconds}`;
}

function formatClockDate(dateValue) {
  if (!(dateValue instanceof Date)) return "--:--:--";
  return dateValue.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
}

export function OvertimePage() {
  const { user } = useAuth();
  const canManageOvertimeApprovals = isManagerRole(user?.role) || isAdminRole(user?.role);
  const isDepartmentManager = isDepartmentManagerRole(user?.role);
  const isOfficeManager = isOfficeManagerRole(user?.role);
  const canCreateOvertime = isEmployeeRole(user?.role) || isDepartmentManagerRole(user?.role);

  const [currentTime, setCurrentTime] = useState(new Date());
  const [loading, setLoading] = useState(true);
  const [isOvertimeLoading, setIsOvertimeLoading] = useState(false);
  const [isSubmittingOvertimeAction, setIsSubmittingOvertimeAction] = useState(false);

  // Overtime data
  const [todayLog, setTodayLog] = useState(null);
  const [overtimeRequests, setOvertimeRequests] = useState([]);
  const [overtimeReports, setOvertimeReports] = useState([]);
  const [todayOvertimeSession, setTodayOvertimeSession] = useState(null);
  const [approvalScopeOvertimeRequests, setApprovalScopeOvertimeRequests] = useState([]);
  const [approvalQueueOvertimeReports, setApprovalQueueOvertimeReports] = useState([]);

  // Modals
  const [isOvertimeRequestModalOpen, setIsOvertimeRequestModalOpen] = useState(false);
  const [isOvertimeReportModalOpen, setIsOvertimeReportModalOpen] = useState(false);

  // Filters
  const [myOvertimeRequestStatusFilter, setMyOvertimeRequestStatusFilter] = useState(OT_FILTER_ALL);
  const [myOvertimeReportStatusFilter, setMyOvertimeReportStatusFilter] = useState(OT_FILTER_ALL);
  const [pendingOvertimeRequestStatusFilter, setPendingOvertimeRequestStatusFilter] = useState(OT_FILTER_ALL);
  const [pendingOvertimeReportStatusFilter, setPendingOvertimeReportStatusFilter] = useState(OT_FILTER_ALL);

  // Forms
  const [overtimeRequestForm] = Form.useForm();
  const [overtimeReportForm] = Form.useForm();

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  // Load today's attendance
  const loadTodayAttendance = async () => {
    setLoading(true);
    try {
      const res = await attendanceService.getToday();
      if (!isSuccessResponse(res)) {
        throw new Error(getResponseMessage(res, "Failed to load attendance"));
      }
      setTodayLog(res?.data ?? null);
    } catch {
      setTodayLog(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTodayAttendance();
  }, []);

  // Load OT data
  const loadOvertimeData = useCallback(async () => {
    setIsOvertimeLoading(true);
    try {
      const [
        myOvertimeRequestsResponse,
        myOvertimeReportsResponse,
        approvalScopeOvertimeRequestsResponse,
        pendingOvertimeReportsResponse,
        todayOvertimeSessionResponse,
      ] = await Promise.all([
        canCreateOvertime ? overtimeService.getMyOvertimeRequests() : Promise.resolve({ data: [] }),
        canCreateOvertime ? overtimeService.getMyOvertimeReports() : Promise.resolve({ data: [] }),
        canManageOvertimeApprovals ? overtimeService.getOvertimeRequestsByApprovalScope() : Promise.resolve({ data: [] }),
        canManageOvertimeApprovals ? overtimeService.getPendingOvertimeReports() : Promise.resolve({ data: [] }),
        canCreateOvertime ? overtimeService.getTodayOvertimeSession() : Promise.resolve({ data: null }),
      ]);

      setOvertimeRequests(
        isSuccessResponse(myOvertimeRequestsResponse) && Array.isArray(myOvertimeRequestsResponse?.data)
          ? myOvertimeRequestsResponse.data
          : []
      );

      setApprovalScopeOvertimeRequests(
        canManageOvertimeApprovals && isSuccessResponse(approvalScopeOvertimeRequestsResponse) && Array.isArray(approvalScopeOvertimeRequestsResponse?.data)
          ? approvalScopeOvertimeRequestsResponse.data
          : []
      );

      setOvertimeReports(
        isSuccessResponse(myOvertimeReportsResponse) && Array.isArray(myOvertimeReportsResponse?.data)
          ? myOvertimeReportsResponse.data
          : []
      );

      setApprovalQueueOvertimeReports(
        canManageOvertimeApprovals && isSuccessResponse(pendingOvertimeReportsResponse) && Array.isArray(pendingOvertimeReportsResponse?.data)
          ? pendingOvertimeReportsResponse.data
          : []
      );

      setTodayOvertimeSession(isSuccessResponse(todayOvertimeSessionResponse) ? (todayOvertimeSessionResponse?.data ?? null) : null);
    } catch {
      toast.error("Failed to load OT data");
    } finally {
      setIsOvertimeLoading(false);
    }
  }, [canManageOvertimeApprovals, canCreateOvertime]);

  useEffect(() => {
    loadOvertimeData();
  }, [loadOvertimeData]);

  // Computed values
  const todayDateKey = useMemo(() => {
    const year = currentTime.getFullYear();
    const month = String(currentTime.getMonth() + 1).padStart(2, "0");
    const day = String(currentTime.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }, [currentTime]);

  const hasApprovedOvertimeRequestForToday = useMemo(() => {
    return overtimeRequests.some(
      (requestItem) => requestItem?.status === OT_STATUS.APPROVED && String(requestItem?.logDate) === todayDateKey
    );
  }, [overtimeRequests, todayDateKey]);

  const hasTodayAttendanceLog = useMemo(
    () => Boolean(todayLog?.logDate) && String(todayLog?.logDate) === todayDateKey,
    [todayLog?.logDate, todayDateKey]
  );

  const isOvertimeSessionCheckedOut = Boolean(todayOvertimeSession?.checkOutTime);
  const isOvertimeSessionCheckedIn = Boolean(todayOvertimeSession?.checkInTime) && !todayOvertimeSession?.checkOutTime;

  const otCheckInDate = useMemo(
    () => parseApiDateTime(todayOvertimeSession?.checkInTime),
    [todayOvertimeSession?.checkInTime],
  );
  const otCheckOutDate = useMemo(
    () => parseApiDateTime(todayOvertimeSession?.checkOutTime),
    [todayOvertimeSession?.checkOutTime],
  );
  const hasInvalidOtTimeline = Boolean(otCheckInDate && otCheckOutDate && otCheckOutDate.getTime() < otCheckInDate.getTime());

  const otElapsedSeconds = useMemo(() => {
    if (!otCheckInDate || hasInvalidOtTimeline) return 0;
    const endDate = otCheckOutDate ?? currentTime;
    const diffMs = endDate.getTime() - otCheckInDate.getTime();
    return Math.max(0, Math.floor(diffMs / 1000));
  }, [currentTime, hasInvalidOtTimeline, otCheckInDate, otCheckOutDate]);

  const otElapsedTimeText = useMemo(() => formatDurationFromSeconds(otElapsedSeconds), [otElapsedSeconds]);
  const otCheckInTimeText = useMemo(() => formatClockDate(otCheckInDate), [otCheckInDate]);
  const otCheckOutTimeText = useMemo(() => formatClockDate(otCheckOutDate), [otCheckOutDate]);
  const minimumOtHours = useMemo(() => {
    const rawMinimum = Number(todayOvertimeSession?.minimumOtHours);
    return Number.isFinite(rawMinimum) && rawMinimum > 0 ? rawMinimum : 1;
  }, [todayOvertimeSession?.minimumOtHours]);
  const minimumOtSeconds = minimumOtHours * 3600;
  const hasReachedMinimumOtDuration = otElapsedSeconds >= minimumOtSeconds;
  const remainingOtSeconds = Math.max(0, minimumOtSeconds - otElapsedSeconds);
  const remainingOtTimeText = useMemo(() => formatDurationFromSeconds(remainingOtSeconds), [remainingOtSeconds]);
  const eligibleOtHours = useMemo(() => Math.max(0, Math.floor(otElapsedSeconds / 3600)), [otElapsedSeconds]);
  const autoReportedOtHours = eligibleOtHours;
  const canCheckOutOvertimeSession = isOvertimeSessionCheckedIn && hasReachedMinimumOtDuration && !hasInvalidOtTimeline;

  const myPendingRequestCount = useMemo(
    () => overtimeRequests.filter((r) => isPendingStatus(r?.status)).length,
    [overtimeRequests],
  );
  const myPendingReportCount = useMemo(
    () => overtimeReports.filter((r) => isPendingStatus(r?.status)).length,
    [overtimeReports],
  );
  const approvalPendingCount = useMemo(
    () =>
      approvalScopeOvertimeRequests.filter((r) => isPendingStatus(r?.status)).length +
      approvalQueueOvertimeReports.filter((r) => isPendingStatus(r?.status)).length,
    [approvalScopeOvertimeRequests, approvalQueueOvertimeReports],
  );
  const approvedOtHours = useMemo(
    () =>
      overtimeReports
        .filter((r) => r?.status === OT_STATUS.APPROVED)
        .reduce((sum, r) => sum + Number(r?.reportedOtHours || 0), 0),
    [overtimeReports],
  );

  // Handlers
  const handleCreateOvertimeRequest = async () => {
    try {
      const values = await overtimeRequestForm.validateFields();
      setIsSubmittingOvertimeAction(true);
      const res = await overtimeService.createOvertimeRequest(values);
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Create OT request failed"));
      }
      toast.success(getResponseMessage(res, "OT request created successfully"));
      setIsOvertimeRequestModalOpen(false);
      overtimeRequestForm.resetFields();
      await loadOvertimeData();
    } catch (e) {
      if (e?.errorFields) return;
      toast.error("Create OT request failed");
    } finally {
      setIsSubmittingOvertimeAction(false);
    }
  };

  const handleOvertimeCheckIn = async () => {
    if (isSubmittingOvertimeAction) return;
    setIsSubmittingOvertimeAction(true);
    try {
      const res = await overtimeService.checkInOvertimeSession();
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "OT check-in failed"));
      }
      toast.success(getResponseMessage(res, "OT checked in successfully"));
      await loadOvertimeData();
    } catch {
      toast.error("OT check-in failed");
    } finally {
      setIsSubmittingOvertimeAction(false);
    }
  };

  const handleOvertimeCheckOut = async () => {
    if (!canCheckOutOvertimeSession) {
      toast.error(
        hasInvalidOtTimeline
          ? "OT session timeline is invalid. Please refresh and try again"
          : `Minimum OT duration is ${minimumOtHours}h. Remaining time: ${remainingOtTimeText}`,
      );
      return;
    }
    if (isSubmittingOvertimeAction) return;
    setIsSubmittingOvertimeAction(true);
    try {
      const res = await overtimeService.checkOutOvertimeSession();
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "OT check-out failed"));
      }
      toast.success(getResponseMessage(res, "OT checked out successfully"));
      await loadOvertimeData();
    } catch {
      toast.error("OT check-out failed");
    } finally {
      setIsSubmittingOvertimeAction(false);
    }
  };

  const handleCreateOvertimeReport = async () => {
    if (!hasTodayAttendanceLog) {
      toast.error("Attendance log is required before submitting OT report");
      return;
    }
    if (!todayOvertimeSession?.id) {
      toast.error("No OT session to submit OT report");
      return;
    }
    try {
      const values = await overtimeReportForm.validateFields();
      setIsSubmittingOvertimeAction(true);
      const res = await overtimeService.createOvertimeReport({
        otSessionId: todayOvertimeSession.id,
        reportNote: values.reportNote,
      });
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Create OT report failed"));
      }
      toast.success(getResponseMessage(res, "OT report submitted successfully"));
      setIsOvertimeReportModalOpen(false);
      overtimeReportForm.resetFields();
      await loadOvertimeData();
    } catch (e) {
      if (e?.errorFields) return;
      toast.error("Create OT report failed");
    } finally {
      setIsSubmittingOvertimeAction(false);
    }
  };

  const handleDecision = async (targetType, targetItem, isApprovedDecision) => {
    if (targetItem?.version == null) {
      toast.error(getOptimisticConflictMessage());
      return;
    }
    try {
      const serviceCall =
        targetType === "request" ? overtimeService.decideOvertimeRequest : overtimeService.decideOvertimeReport;
      const res = await serviceCall(targetItem?.id, {
        approved: isApprovedDecision,
        decisionNote: isApprovedDecision ? "Approved" : "Rejected",
        expectedVersion: targetItem?.version,
      });
      if (!isSuccessResponse(res)) {
        return toast.error(
          isOptimisticConflictResponse(res)
            ? getOptimisticConflictMessage(res)
            : getResponseMessage(res, "Decision failed"),
        );
      }
      toast.success(getResponseMessage(res, "Decision updated"));
      await loadOvertimeData();
    } catch {
      toast.error("Decision failed");
    }
  };

  if (loading) {
    return (
      <div className="rounded-xl p-8" style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
        <p style={{ color: "#8C8C8C", fontSize: "14px" }}>Loading overtime data...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="rounded-2xl p-6" style={{ backgroundColor: "#FFFFFF", border: "1px solid #E8E8E8", boxShadow: "0 1px 3px rgba(0,0,0,0.08)" }}>
        <h1 style={{ fontSize: "28px", fontWeight: "700", color: "#0A0A0A", marginBottom: "8px" }}>Overtime Center</h1>
        <p style={{ fontSize: "14px", color: "#595959", margin: 0 }}>Submit requests, clock OT session, and track approvals in one place.</p>

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-3 mt-4">
          <div className="rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
            <div className="flex items-center gap-2" style={{ color: "#595959", fontSize: "12px", fontWeight: 700 }}>
              <ClipboardList className="w-4 h-4" />
              Pending Requests
            </div>
            <div style={{ fontSize: "24px", fontWeight: 700, color: "#0A0A0A", marginTop: 6 }}>{myPendingRequestCount}</div>
          </div>
          <div className="rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
            <div className="flex items-center gap-2" style={{ color: "#595959", fontSize: "12px", fontWeight: 700 }}>
              <Clock3 className="w-4 h-4" />
              Approved OT Hours
            </div>
            <div style={{ fontSize: "24px", fontWeight: 700, color: "#0A0A0A", marginTop: 6 }}>{approvedOtHours}h</div>
          </div>
          <div className="rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
            <div className="flex items-center gap-2" style={{ color: "#595959", fontSize: "12px", fontWeight: 700 }}>
              <FileCheck2 className="w-4 h-4" />
              Pending Reports
            </div>
            <div style={{ fontSize: "24px", fontWeight: 700, color: "#0A0A0A", marginTop: 6 }}>{myPendingReportCount}</div>
          </div>
          <div className="rounded-xl p-4" style={{ backgroundColor: "#FAFAFA", border: "1px solid #E8E8E8" }}>
            <div className="flex items-center gap-2" style={{ color: "#595959", fontSize: "12px", fontWeight: 700 }}>
              <CheckCircle2 className="w-4 h-4" />
              Queue For Approval
            </div>
            <div style={{ fontSize: "24px", fontWeight: 700, color: "#0A0A0A", marginTop: 6 }}>{canManageOvertimeApprovals ? approvalPendingCount : 0}</div>
          </div>
        </div>

        <div className="mt-4">
          {!hasTodayAttendanceLog ? (
            <Alert type="warning" showIcon message="You need to check-in attendance today before submitting OT report." />
          ) : !hasApprovedOvertimeRequestForToday ? (
            <Alert type="info" showIcon message="Step 1: Create and wait for an approved OT request for today." />
          ) : isOvertimeSessionCheckedOut ? (
            <Alert type="success" showIcon message="OT session completed today. You can submit or review your OT report." />
          ) : isOvertimeSessionCheckedIn ? (
            <Alert type="success" showIcon message="OT session is active. Check-out after completing overtime work." />
          ) : (
            <Alert type="info" showIcon message="Your request is approved. Continue with OT check-in." />
          )}
        </div>
      </div>

      <OvertimeSection
        canCreateOvertime={canCreateOvertime}
        canManageOvertimeApprovals={canManageOvertimeApprovals}
        isDepartmentManager={isDepartmentManager}
        isOfficeManager={isOfficeManager}
        loading={isOvertimeLoading}
        isOvertimeSessionCheckedIn={isOvertimeSessionCheckedIn}
        isOvertimeSessionCheckedOut={isOvertimeSessionCheckedOut}
        otElapsedTimeText={otElapsedTimeText}
        otCheckInTimeText={otCheckInTimeText}
        otCheckOutTimeText={otCheckOutTimeText}
        minimumOtHours={minimumOtHours}
        hasReachedMinimumOtDuration={hasReachedMinimumOtDuration}
        remainingOtTimeText={remainingOtTimeText}
        canCheckOutOvertimeSession={canCheckOutOvertimeSession}
        hasInvalidOtTimeline={hasInvalidOtTimeline}
        hasApprovedOvertimeRequestForToday={hasApprovedOvertimeRequestForToday}
        hasTodayAttendanceLog={hasTodayAttendanceLog}
        myOvertimeRequests={overtimeRequests}
        myOvertimeReports={overtimeReports}
        approvalScopeOvertimeRequests={approvalScopeOvertimeRequests}
        approvalQueueOvertimeReports={approvalQueueOvertimeReports}
        myOvertimeRequestStatusFilter={myOvertimeRequestStatusFilter}
        setMyOvertimeRequestStatusFilter={setMyOvertimeRequestStatusFilter}
        myOvertimeReportStatusFilter={myOvertimeReportStatusFilter}
        setMyOvertimeReportStatusFilter={setMyOvertimeReportStatusFilter}
        pendingOvertimeRequestStatusFilter={pendingOvertimeRequestStatusFilter}
        setPendingOvertimeRequestStatusFilter={setPendingOvertimeRequestStatusFilter}
        pendingOvertimeReportStatusFilter={pendingOvertimeReportStatusFilter}
        setPendingOvertimeReportStatusFilter={setPendingOvertimeReportStatusFilter}
        isSubmittingOvertimeAction={isSubmittingOvertimeAction}
        onRequestNew={() => setIsOvertimeRequestModalOpen(true)}
        onReportNew={() => setIsOvertimeReportModalOpen(true)}
        onCheckIn={handleOvertimeCheckIn}
        onCheckOut={handleOvertimeCheckOut}
        onApproveRequest={(item) => handleDecision("request", item, true)}
        onRejectRequest={(item) => handleDecision("request", item, false)}
        onApproveReport={(item) => handleDecision("report", item, true)}
        onRejectReport={(item) => handleDecision("report", item, false)}
      />

      <Modal
        title="Create OT Request"
        open={isOvertimeRequestModalOpen}
        onCancel={() => setIsOvertimeRequestModalOpen(false)}
        onOk={handleCreateOvertimeRequest}
        okButtonProps={{ loading: isSubmittingOvertimeAction }}
      >
        <Form form={overtimeRequestForm} layout="vertical">
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
        open={isOvertimeReportModalOpen}
        onCancel={() => setIsOvertimeReportModalOpen(false)}
        onOk={handleCreateOvertimeReport}
        okButtonProps={{ loading: isSubmittingOvertimeAction }}
      >
        <Form form={overtimeReportForm} layout="vertical">
          <Form.Item label="Calculated OT Hours">
            <div
              className="rounded-lg px-3 py-2"
              style={{ border: "1px solid #E8E8E8", backgroundColor: "#FAFAFA", color: "#0A0A0A", fontWeight: 600 }}
            >
              {autoReportedOtHours}h (auto-calculated from OT check-in/check-out)
            </div>
          </Form.Item>
          <Form.Item
            name="reportNote"
            label="Report Note"
            extra="Provide specific OT work summary/evidence in 10-500 characters."
            rules={[
              { required: true, message: "Report note (evidence) is required" },
              {
                validator: (_, value) => {
                  const trimmedValue = typeof value === "string" ? value.trim() : "";
                  if (!trimmedValue) {
                    return Promise.reject(new Error("Report note (evidence) is required"));
                  }
                  if (trimmedValue.length < 10) {
                    return Promise.reject(new Error("Report note must be at least 10 characters"));
                  }
                  if (trimmedValue.length > 500) {
                    return Promise.reject(new Error("Report note cannot exceed 500 characters"));
                  }
                  return Promise.resolve();
                },
              },
            ]}
          >
            <Input.TextArea rows={3} maxLength={500} showCount />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}



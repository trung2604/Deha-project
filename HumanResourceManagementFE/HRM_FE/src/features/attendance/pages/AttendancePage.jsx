import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert } from "antd";
import { toast } from "sonner";
import attendanceService from "../api/attendanceService";
import { isSuccessResponse, getResponseMessage } from "@/utils/apiResponse";
import { AttendanceHeader } from "../components/AttendanceHeader";
import { AttendanceCheckPanel } from "../components/AttendanceCheckPanel";
import { AttendanceHistoryTable } from "../components/AttendanceHistoryTable";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isDepartmentManagerRole } from "@/utils/role";

export function AttendancePage() {
  const { user } = useAuth();
  const canViewDepartmentAttendance = isDepartmentManagerRole(user?.role);

  const [currentTime, setCurrentTime] = useState(new Date());
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [todayLog, setTodayLog] = useState(null);
  const [departmentTodayLogs, setDepartmentTodayLogs] = useState([]);
  const [departmentTodayLoading, setDepartmentTodayLoading] = useState(false);

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

  const loadDepartmentToday = useCallback(async () => {
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
    </div>
  );
}


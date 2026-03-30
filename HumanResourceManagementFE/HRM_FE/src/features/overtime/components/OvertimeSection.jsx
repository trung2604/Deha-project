import { Alert, Button, Select, Table, Tag } from "antd";
import { Clock, Plus, CheckCircle, FileText } from "lucide-react";
import {
  getOtStatusMeta,
  OT_FILTER_ALL,
  OT_STATUS,
  OT_STATUS_FILTER_OPTIONS,
} from "../constants/overtimeStatus.constants";

function renderOvertimeStatusTag(status) {
  const meta = getOtStatusMeta(status);
  return <Tag color={meta.color}>{meta.label}</Tag>;
}

export function OvertimeSection({
  canCreateOvertime,
  canManageOvertimeApprovals,
  isDepartmentManager,
  isOfficeManager,
  loading,
  isOvertimeSessionCheckedIn,
  isOvertimeSessionCheckedOut,
  otElapsedTimeText,
  otCheckInTimeText,
  otCheckOutTimeText,
  minimumOtHours,
  hasReachedMinimumOtDuration,
  remainingOtTimeText,
  canCheckOutOvertimeSession,
  hasInvalidOtTimeline,
  hasApprovedOvertimeRequestForToday,
  hasTodayAttendanceLog,
  myOvertimeRequests,
  myOvertimeReports,
  approvalScopeOvertimeRequests,
  approvalQueueOvertimeReports,
  myOvertimeRequestStatusFilter,
  setMyOvertimeRequestStatusFilter,
  myOvertimeReportStatusFilter,
  setMyOvertimeReportStatusFilter,
  pendingOvertimeRequestStatusFilter,
  setPendingOvertimeRequestStatusFilter,
  pendingOvertimeReportStatusFilter,
  setPendingOvertimeReportStatusFilter,
  isSubmittingOvertimeAction,
  onRequestNew,
  onReportNew,
  onCheckIn,
  onCheckOut,
  onApproveRequest,
  onRejectRequest,
  onApproveReport,
  onRejectReport,
}) {
  const filteredMyOvertimeRequests = myOvertimeRequests.filter((r) =>
    myOvertimeRequestStatusFilter === OT_FILTER_ALL ? true : r.status === myOvertimeRequestStatusFilter
  );

  const filteredMyOvertimeReports = myOvertimeReports.filter((r) =>
    myOvertimeReportStatusFilter === OT_FILTER_ALL ? true : r.status === myOvertimeReportStatusFilter
  );

  const filteredApprovalScopeOvertimeRequests = approvalScopeOvertimeRequests.filter((r) =>
    pendingOvertimeRequestStatusFilter === OT_FILTER_ALL ? true : r.status === pendingOvertimeRequestStatusFilter
  );

  const filteredApprovalQueueOvertimeReports = approvalQueueOvertimeReports.filter((r) =>
    pendingOvertimeReportStatusFilter === OT_FILTER_ALL ? true : r.status === pendingOvertimeReportStatusFilter
  );

  const overtimeActionHint = !hasApprovedOvertimeRequestForToday
    ? "You need an approved OT request before OT check-in."
    : isOvertimeSessionCheckedOut
      ? "Session completed. You can submit OT report."
      : isOvertimeSessionCheckedIn && !hasReachedMinimumOtDuration
        ? `Session is active. Minimum OT is ${minimumOtHours}h (remaining ${remainingOtTimeText}).`
      : isOvertimeSessionCheckedIn
        ? "Session is active. Complete your work then check-out."
        : "Ready for OT check-in.";

  return (
    <div className="space-y-5">
      {/* OT Workflow Section */}
      <div className="section-card" style={{ backgroundColor: "#FFFFFF", border: "1px solid #E8E8E8", boxShadow: "0 1px 3px rgba(0,0,0,0.08)" }}>
        <div
          className="section-header section-ot-header"
          style={{ borderColor: "rgba(250, 140, 22, 0.2)" }}
        >
          <div className="section-header-icon" style={{ color: "#fa8c16" }}>
            <Clock className="w-5 h-5" />
          </div>
          <h3 style={{ margin: 0, flex: 1 }}>Overtime Workflow</h3>
          {canCreateOvertime && (
            <button
              type="button"
              onClick={onRequestNew}
              className="flex items-center gap-2 px-4 h-9 rounded-xl transition-all duration-200 hover:opacity-95"
              style={{
                background: "linear-gradient(135deg, #1677FF 0%, #0958D9 100%)",
                color: "#FFFFFF",
                boxShadow: "0 8px 20px rgba(22,119,255,0.26)",
              }}
            >
              <Plus className="w-4 h-4" />
              <span style={{ fontSize: "14px", fontWeight: "500" }}>New Request</span>
            </button>
          )}
        </div>

        <div className="section-content space-y-5" >
          {!hasTodayAttendanceLog && (
            <Alert style={{ marginBottom: "16px" }} type="warning" showIcon message="Please check in attendance for today before submitting OT report." />
          )}

          {/* OT Session Controls */}
          <div className="rounded-xl p-5" style={{ backgroundColor: "rgba(250, 140, 22, 0.04)", border: "1px solid rgba(250, 140, 22, 0.2)" }}>
            <div style={{ color: "#8C8C8C", fontSize: "12px", fontWeight: 700, marginBottom: 10, textTransform: "uppercase" }}>Session Control</div>
            {hasInvalidOtTimeline && (
              <Alert
                type="warning"
                showIcon
                style={{ marginBottom: 12 }}
                message="OT session time data looks invalid. Please refresh or contact admin."
              />
            )}
            <div className="flex items-center gap-3 flex-wrap">
            <div style={{ flex: 1 }}>
              <span style={{ fontSize: "14px", fontWeight: "600", color: "#0a0a0a" }}>
                {isOvertimeSessionCheckedOut ? "✓ OT session completed" : isOvertimeSessionCheckedIn ? "◆ OT session checked in" : "OT session: Not started"}
              </span>
              <div style={{ color: "#8C8C8C", fontSize: "12px", marginTop: 4 }}>{overtimeActionHint}</div>
            </div>
            <div className="flex gap-2">
              <Button
                onClick={onCheckIn}
                disabled={!hasApprovedOvertimeRequestForToday || isOvertimeSessionCheckedIn || isOvertimeSessionCheckedOut}
                loading={isSubmittingOvertimeAction && !isOvertimeSessionCheckedIn}
                icon={<CheckCircle className="w-4 h-4" />}
                size="middle"
                style={{ height: 36 }}
              >
                OT Check-in
              </Button>
              <Button
                onClick={onCheckOut}
                disabled={!canCheckOutOvertimeSession}
                loading={isSubmittingOvertimeAction && isOvertimeSessionCheckedIn}
                size="middle"
                style={{ height: 36 }}
              >
                OT Check-out
              </Button>
              {canCreateOvertime && (
                <Button
                  type="primary"
                  onClick={onReportNew}
                  disabled={!hasApprovedOvertimeRequestForToday || !isOvertimeSessionCheckedOut || !hasTodayAttendanceLog}
                  icon={<FileText className="w-4 h-4" />}
                  size="middle"
                  style={{ height: 36 }}
                >
                  Submit Report
                </Button>
              )}
            </div>
            </div>
            {(isOvertimeSessionCheckedIn || isOvertimeSessionCheckedOut) && (
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mt-4">
                <div className="rounded-lg p-3" style={{ border: "1px solid rgba(22,119,255,0.25)", backgroundColor: "rgba(22,119,255,0.06)" }}>
                  <div style={{ color: "#595959", fontSize: "11px", fontWeight: 700, textTransform: "uppercase", marginBottom: 6 }}>
                    OT Stopwatch
                  </div>
                  <div style={{ fontSize: "24px", fontWeight: 700, color: "#0a0a0a", letterSpacing: 0.5, fontFamily: "monospace" }}>
                    {otElapsedTimeText}
                  </div>
                  <div style={{ color: "#8C8C8C", fontSize: "12px", marginTop: 4 }}>
                    {isOvertimeSessionCheckedIn ? "Running" : "Final duration"}
                  </div>
                  <div style={{ color: "#595959", fontSize: "12px", marginTop: 2 }}>
                    Minimum required: {minimumOtHours}h
                  </div>
                </div>

                <div className="rounded-lg p-3" style={{ border: "1px solid #E8E8E8", backgroundColor: "#FFFFFF" }}>
                  <div style={{ color: "#595959", fontSize: "11px", fontWeight: 700, textTransform: "uppercase", marginBottom: 6 }}>
                    Check-in Time
                  </div>
                  <div style={{ fontSize: "20px", fontWeight: 600, color: "#0a0a0a", fontFamily: "monospace" }}>{otCheckInTimeText}</div>
                </div>

                <div className="rounded-lg p-3" style={{ border: "1px solid #E8E8E8", backgroundColor: "#FFFFFF" }}>
                  <div style={{ color: "#595959", fontSize: "11px", fontWeight: 700, textTransform: "uppercase", marginBottom: 6 }}>
                    Check-out Time
                  </div>
                  <div style={{ fontSize: "20px", fontWeight: 600, color: "#0a0a0a", fontFamily: "monospace" }}>
                    {isOvertimeSessionCheckedOut ? otCheckOutTimeText : "--:--:--"}
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* My OT Requests */}
          {canCreateOvertime && (
            <div className="rounded-xl p-5" style={{ border: "1px solid #E8E8E8", backgroundColor: "#FFFFFF" }}>
              <h4 style={{ fontSize: "13px", fontWeight: "700", color: "#0a0a0a", marginBottom: "12px", textTransform: "uppercase" }}>
                My OT Requests
              </h4>
              <Select
                value={myOvertimeRequestStatusFilter}
                onChange={setMyOvertimeRequestStatusFilter}
                style={{ width: 220, marginBottom: 14 }}
                options={OT_STATUS_FILTER_OPTIONS}
                size="middle"
              />
              <Table
                rowKey={(r) => r.id}
                loading={loading}
                dataSource={filteredMyOvertimeRequests}
                size="middle"
                pagination={{ pageSize: 5 }}
                columns={[
                  { title: "Date", dataIndex: "logDate", key: "logDate", width: 100 },
                  { title: "Reason", dataIndex: "reason", key: "reason", ellipsis: true },
                  { title: "Status", key: "status", width: 120, render: (_, r) => renderOvertimeStatusTag(r.status) },
                ]}
                locale={{ emptyText: "No OT requests yet." }}
              />
            </div>
          )}

          {/* My OT Reports */}
          {canCreateOvertime && (
            <div className="rounded-xl p-5" style={{ border: "1px solid #E8E8E8", backgroundColor: "#FFFFFF" }}>
              <h4 style={{ fontSize: "13px", fontWeight: "700", color: "#0a0a0a", marginBottom: "12px", textTransform: "uppercase" }}>
                My OT Reports
              </h4>
              <Select
                value={myOvertimeReportStatusFilter}
                onChange={setMyOvertimeReportStatusFilter}
                style={{ width: 220, marginBottom: 14 }}
                options={OT_STATUS_FILTER_OPTIONS}
                size="middle"
              />
              <Table
                rowKey={(r) => r.id}
                loading={loading}
                dataSource={filteredMyOvertimeReports}
                size="middle"
                pagination={{ pageSize: 5 }}
                columns={[
                  { title: "Date", dataIndex: "logDate", key: "logDate", width: 110 },
                  { title: "OT Hours", dataIndex: "reportedOtHours", key: "reportedOtHours", width: 100, render: (v) => `${v ?? 0}h` },
                  { title: "Note", dataIndex: "reportNote", key: "reportNote", ellipsis: true },
                  { title: "Status", key: "status", width: 120, render: (_, r) => renderOvertimeStatusTag(r.status) },
                ]}
                locale={{ emptyText: "No OT reports yet." }}
              />
            </div>
          )}

          {/* Approval Queues */}
          {canManageOvertimeApprovals && (
            <>
              <div className="rounded-xl p-5" style={{ border: "1px solid #E8E8E8", backgroundColor: "#FFFFFF" }}>
                <h4 style={{ fontSize: "13px", fontWeight: "700", color: "#0a0a0a", marginBottom: "12px", textTransform: "uppercase" }}>
                  Pending OT Requests (Approval)
                </h4>
                <Select
                  value={pendingOvertimeRequestStatusFilter}
                  onChange={setPendingOvertimeRequestStatusFilter}
                  style={{ width: 220, marginBottom: 14 }}
                    options={OT_STATUS_FILTER_OPTIONS}
                  size="middle"
                />
                <Table
                  rowKey={(r) => r.id}
                  loading={loading}
                  dataSource={filteredApprovalScopeOvertimeRequests}
                  size="middle"
                  pagination={{ pageSize: 5 }}
                  columns={[
                    { title: "User", dataIndex: "userName", key: "userName" },
                    { title: "Date", dataIndex: "logDate", key: "logDate", width: 100 },
                    { title: "Reason", dataIndex: "reason", key: "reason", ellipsis: true },
                    { title: "Status", key: "status", width: 120, render: (_, r) => renderOvertimeStatusTag(r.status) },
                    {
                      title: "Action",
                      key: "action",
                      width: 150,
                      render: (_, r) => {
                        const canDecide =
                          (isDepartmentManager && (r.status === OT_STATUS.PENDING_DEPARTMENT || r.status === OT_STATUS.PENDING)) ||
                          (isOfficeManager && r.status === OT_STATUS.PENDING_OFFICE);
                        if (!canDecide) return <span style={{ color: "#8C8C8C" }}>—</span>;
                        return (
                          <div className="flex gap-2">
                            <Button size="small" type="primary" onClick={() => onApproveRequest(r)}>
                              Approve
                            </Button>
                            <Button size="small" danger onClick={() => onRejectRequest(r)}>
                              Reject
                            </Button>
                          </div>
                        );
                      },
                    },
                  ]}
                  locale={{ emptyText: "No pending OT requests." }}
                />
              </div>

              <div className="rounded-xl p-5" style={{ border: "1px solid #E8E8E8", backgroundColor: "#FFFFFF" }}>
                <h4 style={{ fontSize: "13px", fontWeight: "700", color: "#0a0a0a", marginBottom: "12px", textTransform: "uppercase" }}>
                  Pending OT Reports (Approval)
                </h4>
                <Select
                  value={pendingOvertimeReportStatusFilter}
                  onChange={setPendingOvertimeReportStatusFilter}
                  style={{ width: 220, marginBottom: 14 }}
                    options={OT_STATUS_FILTER_OPTIONS}
                  size="middle"
                />
                <Table
                  rowKey={(r) => r.id}
                  loading={loading}
                  dataSource={filteredApprovalQueueOvertimeReports}
                  size="middle"
                  pagination={{ pageSize: 5 }}
                  columns={[
                    { title: "User", dataIndex: "userName", key: "userName" },
                      { title: "Date", dataIndex: "logDate", key: "logDate", width: 110 },
                    { title: "OT Hours", dataIndex: "reportedOtHours", key: "reportedOtHours", width: 100, render: (v) => `${v ?? 0}h` },
                    { title: "Note", dataIndex: "reportNote", key: "reportNote", ellipsis: true },
                    { title: "Status", key: "status", width: 120, render: (_, r) => renderOvertimeStatusTag(r.status) },
                    {
                      title: "Action",
                      key: "action",
                      width: 150,
                      render: (_, r) => {
                        const canDecide =
                          (isDepartmentManager && (r.status === OT_STATUS.PENDING_DEPARTMENT || r.status === OT_STATUS.PENDING)) ||
                          (isOfficeManager && r.status === OT_STATUS.PENDING_OFFICE);
                        if (!canDecide) return <span style={{ color: "#8C8C8C" }}>—</span>;
                        return (
                          <div className="flex gap-2">
                            <Button size="small" type="primary" onClick={() => onApproveReport(r)}>
                              Approve
                            </Button>
                            <Button size="small" danger onClick={() => onRejectReport(r)}>
                              Reject
                            </Button>
                          </div>
                        );
                      },
                    },
                  ]}
                  locale={{ emptyText: "No pending OT reports." }}
                />
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}



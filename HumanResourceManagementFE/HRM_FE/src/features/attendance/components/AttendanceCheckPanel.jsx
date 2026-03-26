import { Button, Tag } from "antd";
import { LogIn, LogOut } from "lucide-react";

export function AttendanceCheckPanel({
  currentTimeText,
  currentDateText,
  checkedIn,
  checkInTimeText,
  actionLoading,
  onCheckIn,
  onCheckOut,
}) {
  const statusText = checkedIn
    ? `Checked in${checkInTimeText ? ` at ${checkInTimeText}` : ""}`
    : "Not checked in";

  return (
    <div
      className="rounded-xl p-8"
      style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
    >
      <div className="max-w-md mx-auto text-center">
        <div
          className="mb-2"
          style={{
            fontFamily: "DM Sans, sans-serif",
            fontSize: "48px",
            fontWeight: "300",
            color: "#0A0A0A",
            letterSpacing: "0.02em",
          }}
        >
          {currentTimeText}
        </div>
        <div className="mb-6" style={{ color: "#595959", fontSize: "14px" }}>
          {currentDateText}
        </div>

        <div className="mb-6">
          <Tag color={checkedIn ? "success" : "default"} style={{ fontSize: "13px", padding: "4px 10px" }}>
            {statusText}
          </Tag>
        </div>

        <div className="flex items-center gap-4 justify-center">
          {!checkedIn ? (
            <Button
              type="primary"
              size="large"
              onClick={onCheckIn}
              loading={actionLoading}
              icon={<LogIn className="w-5 h-5" />}
              className="px-8"
            >
              Check In
            </Button>
          ) : (
            <Button
              danger
              size="large"
              onClick={onCheckOut}
              loading={actionLoading}
              icon={<LogOut className="w-5 h-5" />}
              className="px-8"
            >
              Check Out
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}


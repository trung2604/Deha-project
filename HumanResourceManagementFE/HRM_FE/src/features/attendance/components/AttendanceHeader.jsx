import { Clock } from "lucide-react";

export function AttendanceHeader() {
  return (
    <div className="mb-6">
      <div className="flex items-center gap-3 mb-2">
        <div
          className="p-2 rounded-lg"
          style={{
            backgroundColor: "rgba(22, 119, 255, 0.1)",
            color: "#1677ff",
          }}
        >
          <Clock className="w-6 h-6" />
        </div>
        <h1
          style={{
            fontFamily: "DM Sans, sans-serif",
            fontSize: "28px",
            fontWeight: "700",
            color: "#0A0A0A",
            margin: 0,
          }}
        >
          Attendance
        </h1>
      </div>
      <p
        style={{
          fontSize: "14px",
          color: "#595959",
          margin: 0,
          paddingLeft: "44px",
        }}
      >
        Track your daily attendance and work hours
      </p>
    </div>
  );
}


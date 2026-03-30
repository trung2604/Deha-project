import { Calendar } from "lucide-react";
import { Empty, Tag } from "antd";

function formatDate(value) {
  if (!value) return "-";
  return String(value).slice(0, 10);
}

function formatTime(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").slice(11, 16);
}

function deriveStatus(item) {
  if (!item?.checkInTime) return "Absent";
  const time = formatTime(item.checkInTime);
  if (time === "-") return "Absent";
  const [h, m] = time.split(":").map((v) => Number(v || 0));
  if (h > 9 || (h === 9 && m > 0)) return "Late";
  return "On Time";
}

function toDurationText(item) {
  const worked = typeof item?.workedHours === "number" ? item.workedHours : null;
  const ot = typeof item?.otHours === "number" ? item.otHours : null;
  if (worked == null && ot == null) return "-";
  if ((ot ?? 0) > 0) return `${worked ?? 0}h (+${ot}h OT)`;
  return `${worked ?? 0}h`;
}

export function AttendanceHistoryTable({ records, title = "Attendance History" }) {
  const hasRecords = Array.isArray(records) && records.length > 0;

  return (
    <div className="section-card">
      {/* Header */}
      <div
        className="section-header section-attendance-header"
        style={{ borderColor: "rgba(22, 119, 255, 0.2)" }}
      >
        <div className="section-header-icon">
          <Calendar className="w-5 h-5" />
        </div>
        <h3 style={{ margin: 0, flex: 1 }}>{title}</h3>
      </div>

      {/* Table */}
      <div className="section-content">
        <div className="overflow-x-auto">
          <table className="enhanced-table">
            <thead>
              <tr>
                {["Date", "Day", "Check-in", "Check-out", "Duration", "Status"].map((title) => (
                  <th key={title}>{title}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {!hasRecords ? (
                <tr style={{ height: "200px" }}>
                  <td colSpan={6} style={{ padding: "40px" }}>
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No attendance records" />
                  </td>
                </tr>
              ) : (
                records.map((record) => {
                  const status = deriveStatus(record);
                  const day = record?.logDate
                    ? new Date(`${record.logDate}T00:00:00`).toLocaleDateString("en-US", { weekday: "short" })
                    : "-";
                  return (
                    <tr key={record.id || record.logDate || "today"}>
                      <td style={{ fontWeight: "600" }}>{formatDate(record?.logDate)}</td>
                      <td className="text-secondary">{day}</td>
                      <td>{formatTime(record?.checkInTime)}</td>
                      <td>{formatTime(record?.checkOutTime)}</td>
                      <td className="text-secondary">{toDurationText(record)}</td>
                      <td>
                        <Tag
                          color={status === "On Time" ? "success" : status === "Late" ? "warning" : "error"}
                          style={{ fontWeight: "500" }}
                        >
                          {status}
                        </Tag>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}


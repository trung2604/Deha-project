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
    <div
      className="rounded-xl overflow-hidden"
      style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
    >
      <div className="p-6 border-b flex items-center justify-between" style={{ borderColor: "#E8E8E8" }}>
        <h3
          style={{
            fontFamily: "DM Sans, sans-serif",
            fontSize: "16px",
            fontWeight: "600",
            color: "#0A0A0A",
          }}
        >
          {title}
        </h3>
        <button
          type="button"
          disabled
          className="flex items-center gap-2 px-3 h-8 rounded-lg border transition-colors duration-150 opacity-60"
          style={{ borderColor: "#E8E8E8", color: "#0A0A0A" }}
        >
          <Calendar className="w-4 h-4" />
          <span style={{ fontSize: "13px", fontWeight: "500" }}>Date Range</span>
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full">
          <thead style={{ backgroundColor: "#F5F7FA" }}>
            <tr>
              {["Date", "Day", "Check-in Time", "Check-out Time", "Duration", "Status"].map((title) => (
                <th
                  key={title}
                  className="px-6 py-3 text-left uppercase tracking-wide"
                  style={{ color: "#595959", fontSize: "11px", fontWeight: "600" }}
                >
                  {title}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {!hasRecords ? (
              <tr className="border-t" style={{ borderColor: "#E8E8E8", height: "56px" }}>
                <td colSpan={6} className="px-6 py-6" style={{ color: "#8C8C8C", fontSize: "14px" }}>
                  <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No attendance records available" />
                </td>
              </tr>
            ) : (
              records.map((record) => {
                const status = deriveStatus(record);
                const day = record?.logDate
                  ? new Date(`${record.logDate}T00:00:00`).toLocaleDateString("en-US", { weekday: "long" })
                  : "-";
                return (
                  <tr
                    key={record.id || record.logDate || "today"}
                    className="border-t transition-colors duration-150 hover:bg-blue-50/30"
                    style={{ borderColor: "#E8E8E8", height: "56px" }}
                  >
                    <td className="px-6 py-4" style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: "500" }}>
                      {formatDate(record?.logDate)}
                    </td>
                    <td className="px-6 py-4" style={{ color: "#595959", fontSize: "14px" }}>
                      {day}
                    </td>
                    <td className="px-6 py-4" style={{ color: "#0A0A0A", fontSize: "14px" }}>
                      {formatTime(record?.checkInTime)}
                    </td>
                    <td className="px-6 py-4" style={{ color: "#0A0A0A", fontSize: "14px" }}>
                      {formatTime(record?.checkOutTime)}
                    </td>
                    <td className="px-6 py-4" style={{ color: "#595959", fontSize: "14px" }}>
                      {toDurationText(record)}
                    </td>
                    <td className="px-6 py-4">
                      <Tag
                        color={status === "On Time" ? "success" : status === "Late" ? "warning" : "error"}
                        style={{ fontSize: "12px", fontWeight: "500" }}
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
  );
}


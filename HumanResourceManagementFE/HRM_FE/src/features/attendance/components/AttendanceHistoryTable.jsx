import { useMemo, useState } from "react";
import { Calendar } from "lucide-react";
import { Empty, Input, Select, Table, Tag } from "antd";

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
  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const safeRecords = Array.isArray(records) ? records : [];

  const hasUserColumn = useMemo(
    () => safeRecords.some((item) => item?.userName),
    [safeRecords],
  );

  const filteredRecords = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();
    return safeRecords.filter((item) => {
      const status = deriveStatus(item);
      const statusMatched = statusFilter === "ALL" ? true : status === statusFilter;
      if (!statusMatched) return false;
      if (!keyword) return true;

      const haystack = [
        item?.userName,
        item?.logDate,
        formatTime(item?.checkInTime),
        formatTime(item?.checkOutTime),
        toDurationText(item),
        status,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return haystack.includes(keyword);
    });
  }, [safeRecords, searchText, statusFilter]);

  const columns = useMemo(() => {
    const baseColumns = [
      {
        title: "Date",
        dataIndex: "logDate",
        key: "logDate",
        sorter: (a, b) => String(a?.logDate || "").localeCompare(String(b?.logDate || "")),
        defaultSortOrder: "descend",
        render: (value) => <span style={{ fontWeight: 600 }}>{formatDate(value)}</span>,
      },
      {
        title: "Day",
        key: "day",
        sorter: (a, b) => {
          const dayA = a?.logDate
            ? new Date(`${a.logDate}T00:00:00`).toLocaleDateString("en-US", { weekday: "short" })
            : "";
          const dayB = b?.logDate
            ? new Date(`${b.logDate}T00:00:00`).toLocaleDateString("en-US", { weekday: "short" })
            : "";
          return dayA.localeCompare(dayB);
        },
        render: (_, record) => {
          const day = record?.logDate
            ? new Date(`${record.logDate}T00:00:00`).toLocaleDateString("en-US", { weekday: "short" })
            : "-";
          return <span className="text-secondary">{day}</span>;
        },
      },
      {
        title: "Check-in",
        dataIndex: "checkInTime",
        key: "checkInTime",
        sorter: (a, b) => String(a?.checkInTime || "").localeCompare(String(b?.checkInTime || "")),
        render: (value) => formatTime(value),
      },
      {
        title: "Check-out",
        dataIndex: "checkOutTime",
        key: "checkOutTime",
        sorter: (a, b) => String(a?.checkOutTime || "").localeCompare(String(b?.checkOutTime || "")),
        render: (value) => formatTime(value),
      },
      {
        title: "Duration",
        key: "duration",
        sorter: (a, b) => Number(a?.workedHours || 0) + Number(a?.otHours || 0) - (Number(b?.workedHours || 0) + Number(b?.otHours || 0)),
        render: (_, record) => <span className="text-secondary">{toDurationText(record)}</span>,
      },
      {
        title: "Status",
        key: "status",
        sorter: (a, b) => deriveStatus(a).localeCompare(deriveStatus(b)),
        render: (_, record) => {
          const status = deriveStatus(record);
          return (
            <Tag
              color={status === "On Time" ? "success" : status === "Late" ? "warning" : "error"}
              style={{ fontWeight: "500" }}
            >
              {status}
            </Tag>
          );
        },
      },
    ];

    if (!hasUserColumn) return baseColumns;

    return [
      {
        title: "User",
        dataIndex: "userName",
        key: "userName",
        sorter: (a, b) => String(a?.userName || "").localeCompare(String(b?.userName || "")),
      },
      ...baseColumns,
    ];
  }, [hasUserColumn]);

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
        <div className="flex flex-wrap gap-3 mb-4">
          <Input
            allowClear
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            placeholder="Search by date, user, time, status..."
            style={{ width: 300 }}
          />
          <Select
            value={statusFilter}
            onChange={setStatusFilter}
            style={{ width: 180 }}
            options={[
              { value: "ALL", label: "All Statuses" },
              { value: "On Time", label: "On Time" },
              { value: "Late", label: "Late" },
              { value: "Absent", label: "Absent" },
            ]}
          />
        </div>

        <Table
          rowKey={(record) => record.id || `${record.logDate}-${record.userName || "me"}`}
          dataSource={filteredRecords}
          columns={columns}
          pagination={{ pageSize: 10 }}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description={safeRecords.length ? "No records match your search/filter" : "No attendance records"}
              />
            ),
          }}
          size="middle"
        />
      </div>
    </div>
  );
}


import { Button, Table, Tag } from "antd";
import { FileText } from "lucide-react";
import { payrollSmallPrimaryButtonStyle } from "../constants/buttonStyles";

function currency(value) {
  if (value == null) return "-";
  return `${Number(value).toLocaleString()} VND`;
}

function statusColor(status) {
  if (status === "PAID") return "success";
  if (status === "FINALIZED") return "processing";
  return "default";
}

export function PayrollTable({ loading, payrolls, onViewDetail }) {
  const columns = [
    {
      title: "User",
      dataIndex: "userName",
      key: "userName",
      width: 160,
      render: (value) => (
        <span style={{ fontWeight: "600" }}>{value || "-"}</span>
      ),
    },
    {
      title: "Period",
      key: "period",
      width: 100,
      sorter: (a, b) => (a.year - b.year) || (a.month - b.month),
      render: (_, r) => (
        <span style={{ fontWeight: "600", color: "#1677ff" }}>
          {r.year}-{String(r.month).padStart(2, "0")}
        </span>
      ),
    },
    {
      title: "Regular Hours",
      dataIndex: "regularHours",
      key: "regularHours",
      width: 120,
      align: "right",
      render: (v) => (v != null ? `${v}h` : "-"),
    },
    {
      title: "Regular Pay",
      dataIndex: "regularPay",
      key: "regularPay",
      width: 140,
      align: "right",
      render: (v) => <span style={{ color: "#52c41a", fontWeight: "600" }}>{currency(v)}</span>,
    },
    {
      title: "OT Hours",
      dataIndex: "otHours",
      key: "otHours",
      width: 100,
      align: "right",
      render: (v) => (v != null ? <span style={{ color: "#fa8c16" }}>{v}h</span> : "-"),
    },
    {
      title: "OT Pay",
      dataIndex: "otPay",
      key: "otPay",
      width: 140,
      align: "right",
      render: (v) => <span style={{ color: "#fa8c16", fontWeight: "600" }}>{currency(v)}</span>,
    },
    {
      title: "Net Salary",
      dataIndex: "netSalary",
      key: "netSalary",
      width: 150,
      align: "right",
      render: (v) => (
        <span style={{ color: "#52c41a", fontWeight: "700", fontSize: "15px" }}>
          {currency(v)}
        </span>
      ),
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (v) => (v ? <Tag color={statusColor(v)}>{v}</Tag> : "-"),
    },
    {
      title: "Action",
      key: "action",
      width: 100,
      fixed: "right",
      render: (_, r) => (
        <Button
          type="primary"
          size="small"
          icon={<FileText className="w-4 h-4" />}
          className="rounded-lg transition-all duration-200 hover:opacity-95"
          style={payrollSmallPrimaryButtonStyle}
          onClick={() => onViewDetail(r)}
        >
          Detail
        </Button>
      ),
    },
  ];

  return (
    <div className="section-card">
      <div
        className="section-header section-payroll-header"
        style={{ borderColor: "rgba(82, 196, 26, 0.2)" }}
      >
        <div className="section-header-icon">
          <FileText className="w-5 h-5" style={{ color: "#52c41a" }} />
        </div>
        <h3 style={{ margin: 0 }}>Payroll Records</h3>
      </div>
      <div className="section-content" style={{ padding: 0 }}>
        <Table
          rowKey={(r) => r.id}
          loading={loading}
          dataSource={payrolls}
          columns={columns}
          scroll={{ x: 1180 }}
          pagination={{ pageSize: 10, size: "small" }}
          locale={{ emptyText: "No payroll records. Try generate payroll for selected filters." }}
          style={{ overflow: "hidden" }}
        />
      </div>
    </div>
  );
}


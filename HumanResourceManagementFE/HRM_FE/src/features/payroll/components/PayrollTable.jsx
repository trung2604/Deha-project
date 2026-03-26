import { Button, Table, Tag } from "antd";

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
      render: (value) => value || "-",
    },
    {
      title: "Period",
      key: "period",
      sorter: (a, b) => (a.year - b.year) || (a.month - b.month),
      render: (_, r) => `${r.year}-${String(r.month).padStart(2, "0")}`,
    },
    {
      title: "Regular Pay",
      dataIndex: "regularPay",
      key: "regularPay",
      render: (v) => currency(v),
    },
    {
      title: "Regular Hours",
      dataIndex: "regularHours",
      key: "regularHours",
      render: (v) => (v != null ? `${v}h` : "-"),
    },
    {
      title: "Approved OT Hours",
      dataIndex: "otHours",
      key: "otHours",
      render: (v) => (v != null ? `${v}h` : "-"),
    },
    {
      title: "Approved OT Pay",
      dataIndex: "otPay",
      key: "otPay",
      render: (v) => currency(v),
    },
    {
      title: "Net",
      dataIndex: "netSalary",
      key: "netSalary",
      render: (v) => currency(v),
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      render: (v) => (v ? <Tag color={statusColor(v)}>{v}</Tag> : "-"),
    },
    {
      title: "Action",
      key: "action",
      render: (_, r) => (
        <Button size="small" onClick={() => onViewDetail(r)}>
          Detail
        </Button>
      ),
    },
  ];

  return (
    <div className="rounded-xl overflow-hidden" style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
      <Table
        rowKey={(r) => r.id}
        loading={loading}
        dataSource={payrolls}
        columns={columns}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: "No payroll records. Try generate payroll for selected filters." }}
      />
    </div>
  );
}


import { Descriptions, Drawer, Tag } from "antd";

function n(value) {
  if (value == null) return "-";
  return `${Number(value).toLocaleString()} VND`;
}

function statusColor(status) {
  if (status === "PAID") return "success";
  if (status === "FINALIZED") return "processing";
  return "default";
}

export function PayrollDetailDrawer({ open, payroll, onClose }) {
  return (
    <Drawer title="Payroll Detail" open={open} onClose={onClose} width={640}>
      {!payroll ? null : (
        <Descriptions bordered column={1}>
          <Descriptions.Item label="User">{payroll.userName || "-"}</Descriptions.Item>
          <Descriptions.Item label="Period">{`${payroll.year}-${String(payroll.month).padStart(2, "0")}`}</Descriptions.Item>
          <Descriptions.Item label="Base Salary Snapshot">{n(payroll.baseSalarySnapshot)}</Descriptions.Item>
          <Descriptions.Item label="Working Days">{payroll.workingDaysInMonth ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Present Days">{payroll.presentDays ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="Regular Hours">{payroll.regularHours != null ? `${payroll.regularHours}h` : "-"}</Descriptions.Item>
          <Descriptions.Item label="Regular Pay">{n(payroll.regularPay)}</Descriptions.Item>
          <Descriptions.Item label="Approved OT Hours">{payroll.otHours != null ? `${payroll.otHours}h` : "-"}</Descriptions.Item>
          <Descriptions.Item label="Approved OT Pay">{n(payroll.otPay)}</Descriptions.Item>
          <Descriptions.Item label="Net Salary">{n(payroll.netSalary)}</Descriptions.Item>
          <Descriptions.Item label="Status">{payroll.status ? <Tag color={statusColor(payroll.status)}>{payroll.status}</Tag> : "-"}</Descriptions.Item>
          <Descriptions.Item label="Generated At">{payroll.generatedAt ? String(payroll.generatedAt).replace("T", " ").slice(0, 19) : "-"}</Descriptions.Item>
        </Descriptions>
      )}
    </Drawer>
  );
}


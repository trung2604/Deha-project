import { Drawer, Tag, Row, Col, Divider } from "antd";
import { FileText } from "lucide-react";

function n(value) {
  if (value == null) return "-";
  return `${Number(value).toLocaleString()} VND`;
}

function statusColor(status) {
  if (status === "PAID") return "success";
  if (status === "FINALIZED") return "processing";
  return "default";
}

function DetailRow({ label, value, color = "text-primary", bold = false }) {
  return (
    <div style={{ marginBottom: "16px" }}>
      <div style={{ fontSize: "12px", fontWeight: "600", color: "#595959", textTransform: "uppercase", marginBottom: "6px" }}>
        {label}
      </div>
      <div
        style={{
          fontSize: "16px",
          fontWeight: bold ? "700" : "500",
          color: color === "success" ? "#52c41a" : color === "warning" ? "#fa8c16" : "#0a0a0a",
        }}
      >
        {value}
      </div>
    </div>
  );
}

export function PayrollDetailDrawer({ open, payroll, onClose }) {
  return (
    <Drawer
      title={
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <FileText style={{ color: "#52c41a" }} size={20} />
          <span>Payroll Details</span>
        </div>
      }
      open={open}
      onClose={onClose}
      width={720}
      bodyStyle={{ paddingBottom: 80 }}
    >
      {!payroll ? null : (
        <div>
          {/* Header Section */}
          <div style={{ marginBottom: "32px" }}>
            <DetailRow label="Employee Name" value={payroll.userName || "-"} bold />
            <div style={{ display: "flex", gap: "16px", marginTop: "16px" }}>
              <div style={{ flex: 1 }}>
                <DetailRow label="Period" value={`${payroll.year}-${String(payroll.month).padStart(2, "0")}`} />
              </div>
              <div style={{ flex: 1 }}>
                <DetailRow
                  label="Status"
                  value={payroll.status ? <Tag color={statusColor(payroll.status)}>{payroll.status}</Tag> : "-"}
                />
              </div>
            </div>
          </div>

          <Divider style={{ margin: "24px 0" }} />

          {/* Work Summary Section */}
          <div style={{ marginBottom: "32px" }}>
            <h3 style={{ fontSize: "14px", fontWeight: "700", color: "#0a0a0a", marginBottom: "16px", textTransform: "uppercase" }}>
              Work Summary
            </h3>
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <DetailRow label="Working Days" value={payroll.workingDaysInMonth ?? "-"} />
              </Col>
              <Col span={12}>
                <DetailRow label="Present Days" value={payroll.presentDays ?? "-"} />
              </Col>
              <Col span={12}>
                <DetailRow label="Base Salary" value={n(payroll.baseSalarySnapshot)} />
              </Col>
              <Col span={12}>
                <DetailRow label="Regular Hours" value={payroll.regularHours != null ? `${payroll.regularHours}h` : "-"} />
              </Col>
            </Row>
          </div>

          <Divider style={{ margin: "24px 0" }} />

          {/* Compensation Section */}
          <div style={{ marginBottom: "32px" }}>
            <h3 style={{ fontSize: "14px", fontWeight: "700", color: "#0a0a0a", marginBottom: "16px", textTransform: "uppercase" }}>
              Compensation
            </h3>
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <DetailRow label="Regular Pay" value={n(payroll.regularPay)} color="success" bold />
              </Col>
              <Col span={12}>
                <DetailRow label="Overtime Hours" value={payroll.otHours != null ? `${payroll.otHours}h` : "-"} color="warning" />
              </Col>
              <Col span={12}>
                <DetailRow label="Overtime Pay" value={n(payroll.otPay)} color="warning" />
              </Col>
            </Row>
          </div>

          <Divider style={{ margin: "24px 0", borderColor: "#52c41a", borderWidth: "2px" }} />

          {/* Net Salary */}
          <div style={{ marginBottom: "32px", padding: "16px", backgroundColor: "rgba(82, 196, 26, 0.05)", borderRadius: "8px" }}>
            <DetailRow label="NET SALARY" value={n(payroll.netSalary)} color="success" bold />
          </div>

          {/* Metadata */}
          <div style={{ marginTop: "32px", padding: "12px", backgroundColor: "#f5f7fa", borderRadius: "8px" }}>
            <DetailRow
              label="Generated At"
              value={payroll.generatedAt ? String(payroll.generatedAt).replace("T", " ").slice(0, 19) : "-"}
            />
          </div>
        </div>
      )}
    </Drawer>
  );
}


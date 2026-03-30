import { Button, Form, Select } from "antd";
import { Zap } from "lucide-react";
import { payrollPrimaryButtonStyle } from "../constants/buttonStyles";

export function GeneratePayrollPanel({
  submitting,
  offices,
  users,
  showOfficeFilter,
  initialYear,
  initialMonth,
  initialOfficeId,
  onSubmit,
}) {
  const [form] = Form.useForm();

  const yearOptions = Array.from({ length: 7 }).map((_, i) => {
    const y = new Date().getFullYear() - 3 + i;
    return { value: y, label: String(y) };
  });
  const monthOptions = Array.from({ length: 12 }).map((_, i) => ({
    value: i + 1,
    label: `Month ${i + 1}`,
  }));

  return (
    <div className="section-card mb-6">
      <div
        className="section-header section-payroll-header"
        style={{ borderColor: "rgba(82, 196, 26, 0.2)" }}
      >
        <div className="section-header-icon">
          <Zap className="w-5 h-5" style={{ color: "#52c41a" }} />
        </div>
        <h3 style={{ margin: 0 }}>Generate Payroll</h3>
      </div>

      <div className="section-content">
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            year: initialYear,
            month: initialMonth,
            officeId: initialOfficeId,
            userId: undefined,
          }}
          onFinish={onSubmit}
        >
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
            <Form.Item
              name="year"
              label={<span className="form-label">Year</span>}
              rules={[{ required: true, message: "Year is required" }]}
              style={{ marginBottom: 0 }}
            >
              <Select options={yearOptions} size="large" />
            </Form.Item>
            <Form.Item
              name="month"
              label={<span className="form-label">Month</span>}
              rules={[{ required: true, message: "Month is required" }]}
              style={{ marginBottom: 0 }}
            >
              <Select options={monthOptions} size="large" />
            </Form.Item>
            {showOfficeFilter && (
              <Form.Item
                name="officeId"
                label={<span className="form-label">Office</span>}
                style={{ marginBottom: 0 }}
              >
                <Select
                  allowClear
                  placeholder="All offices"
                  size="large"
                  options={offices.map((o) => ({ value: o.id, label: o.name }))}
                />
              </Form.Item>
            )}
            <Form.Item
              name="userId"
              label={<span className="form-label">User</span>}
              style={{ marginBottom: 0 }}
            >
              <Select
                allowClear
                placeholder="All users in scope"
                size="large"
                options={users.map((u) => ({
                  value: u.id,
                  label: `${u.firstName ?? ""} ${u.lastName ?? ""}`.trim() || u.email,
                }))}
              />
            </Form.Item>
          </div>
          <div className="mt-6">
            <Button
              type="primary"
              htmlType="submit"
              loading={submitting}
              size="large"
              className="rounded-xl transition-all duration-200 hover:opacity-95"
              style={{ minWidth: "170px", ...payrollPrimaryButtonStyle }}
            >
              Generate Payroll
            </Button>
          </div>
        </Form>
      </div>
    </div>
  );
}


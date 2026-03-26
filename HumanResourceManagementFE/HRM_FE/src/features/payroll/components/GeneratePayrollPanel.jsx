import { Button, Form, Select } from "antd";

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
    <div className="rounded-xl p-4" style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
      <h3 style={{ fontSize: "16px", fontWeight: 600, color: "#0A0A0A", marginBottom: 12 }}>Generate Payroll</h3>

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
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-3">
          <Form.Item name="year" label="Year" rules={[{ required: true, message: "Year is required" }]}>
            <Select options={yearOptions} />
          </Form.Item>
          <Form.Item name="month" label="Month" rules={[{ required: true, message: "Month is required" }]}>
            <Select options={monthOptions} />
          </Form.Item>
          {showOfficeFilter && (
            <Form.Item name="officeId" label="Office">
              <Select allowClear placeholder="All offices" options={offices.map((o) => ({ value: o.id, label: o.name }))} />
            </Form.Item>
          )}
          <Form.Item name="userId" label="User">
            <Select
              allowClear
              placeholder="All users in scope"
              options={users.map((u) => ({ value: u.id, label: `${u.firstName ?? ""} ${u.lastName ?? ""}`.trim() || u.email }))}
            />
          </Form.Item>
        </div>
        <Button type="primary" htmlType="submit" loading={submitting}>
          Generate
        </Button>
      </Form>
    </div>
  );
}


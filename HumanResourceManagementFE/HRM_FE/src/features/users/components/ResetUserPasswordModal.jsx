import { Form, Input, Modal } from "antd";
import { useState } from "react";
export function ResetUserPasswordModal({ user, onClose, onSubmit }) {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const handleOk = async () => {
    const values = await form.validateFields();
    if (values.newPassword !== values.confirmPassword) {
      form.setFields([{ name: "confirmPassword", errors: ["Password confirmation does not match"] }]);
      return;
    }
    setSubmitting(true);
    try {
      await onSubmit(values.newPassword);
      form.resetFields();
    } finally {
      setSubmitting(false);
    }
  };
  return (
    <Modal
      open={Boolean(user)}
      title={`Reset Password${user ? ` - ${user.firstName ?? ""} ${user.lastName ?? ""}` : ""}`}
      onCancel={onClose}
      onOk={handleOk}
      okText="Reset Password"
      okButtonProps={{ loading: submitting }}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="New Password"
          name="newPassword"
          rules={[
            { required: true, message: "New password is required" },
            { min: 8, message: "Password must be at least 8 characters" },
          ]}
        >
          <Input.Password placeholder="Enter new password" />
        </Form.Item>
        <Form.Item
          label="Confirm Password"
          name="confirmPassword"
          rules={[{ required: true, message: "Password confirmation is required" }]}
        >
          <Input.Password placeholder="Re-enter new password" />
        </Form.Item>
      </Form>
    </Modal>
  );
}

import { DatePicker, Form, InputNumber, Modal, Select } from "antd";
import dayjs from "dayjs";
import { useEffect } from "react";
import { payrollPrimaryButtonStyle } from "../constants/buttonStyles";

export function SalaryContractModal({
                                        open,
                                        submitting,
                                        users,
                                        editingContract,
                                        defaultUserId,
                                        onSubmit,
                                        onClose,
                                    }) {
    const [form] = Form.useForm();

    // Mỗi khi modal mở hoặc editingContract thay đổi → set lại toàn bộ fields
    useEffect(() => {
        if (open) {
            form.setFieldsValue({
                userId:     editingContract?.userId     ?? defaultUserId ?? undefined,
                baseSalary: editingContract?.baseSalary ?? undefined,
                startDate:  editingContract?.startDate  ? dayjs(editingContract.startDate) : null,
                endDate:    editingContract?.endDate    ? dayjs(editingContract.endDate)   : null,
            });
        }
    }, [defaultUserId, editingContract, form, open]);

    return (
        <Modal
            title={editingContract ? "Edit Salary Contract" : "Create Salary Contract"}
            open={open}
            onCancel={onClose}
            onOk={() => form.submit()}
            okButtonProps={{
                loading: submitting,
                className: "rounded-xl transition-all duration-200 hover:opacity-95",
                style: payrollPrimaryButtonStyle,
            }}
            destroyOnHidden
        >
            <Form
                form={form}
                layout="vertical"
                onFinish={(values) => {
                    onSubmit({
                        userId:     values.userId,
                        baseSalary: values.baseSalary,
                        startDate:  values.startDate?.format("YYYY-MM-DD"),
                        endDate:    values.endDate?.format("YYYY-MM-DD") ?? null,
                        expectedVersion: editingContract?.version,
                    });
                }}
            >
                <Form.Item name="userId" label="User" rules={[{ required: true, message: "User is required" }]}>
                    <Select
                        showSearch
                        optionFilterProp="label"
                        options={users.map((u) => ({
                            value: u.id,
                            label: `${u.firstName ?? ""} ${u.lastName ?? ""}`.trim() || u.email,
                        }))}
                    />
                </Form.Item>
                <Form.Item name="baseSalary" label="Base Salary" rules={[{ required: true, message: "Base salary is required" }]}>
                    <InputNumber
                        min={1}
                        style={{ width: "100%" }}
                        formatter={(value) => (value ? `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",") : "")}
                        parser={(value) => value?.replace(/,/g, "")}
                        placeholder="Enter monthly base salary"
                    />
                </Form.Item>
                <Form.Item name="startDate" label="Start Date" rules={[{ required: true, message: "Start date is required" }]}>
                    <DatePicker style={{ width: "100%" }} />
                </Form.Item>
                <Form.Item name="endDate" label="End Date">
                    <DatePicker
                        style={{ width: "100%" }}
                        disabledDate={(current) => {
                            const startDate = form.getFieldValue("startDate");
                            if (!startDate || !current) return false;
                            return current.isBefore(startDate, "day");
                        }}
                    />
                </Form.Item>
            </Form>
        </Modal>
    );
}
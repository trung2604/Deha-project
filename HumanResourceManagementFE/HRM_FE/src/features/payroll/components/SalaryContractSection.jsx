import { useMemo, useState } from "react";
import { Button, DatePicker, Input, Select, Space, Table, Tag } from "antd";
import dayjs from "dayjs";

function currency(value) {
  if (value == null) return "-";
  return `${Number(value).toLocaleString()} VND`;
}

function statusMeta(contract) {
  const backendStatus = String(contract?.status ?? "").toUpperCase();
  if (backendStatus === "ACTIVE")
    return { value: "active", label: "Active", color: "success" };
  if (backendStatus === "FUTURE")
    return { value: "future", label: "Future", color: "blue" };
  if (backendStatus === "EXPIRED")
    return { value: "expired", label: "Expired", color: "default" };

  const today = dayjs().startOf("day");
  const start = contract?.startDate ? dayjs(contract.startDate) : null;
  const end = contract?.endDate ? dayjs(contract.endDate) : null;
  if (!start) return { value: "unknown", label: "Unknown", color: "default" };
  if (start.isAfter(today, "day"))
    return { value: "future", label: "Future", color: "blue" };
  if (end && end.isBefore(today, "day"))
    return { value: "expired", label: "Expired", color: "default" };
  return { value: "active", label: "Active", color: "success" };
}

export function SalaryContractSection({
  loading,
  contracts,
  users,
  selectedUserId,
  onUserChange,
  onCreate,
  onEdit,
}) {
  const [keyword, setKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [range, setRange] = useState(null);

  const filteredContracts = useMemo(() => {
    const from = range?.[0] ? dayjs(range[0]).startOf("day") : null;
    const to = range?.[1] ? dayjs(range[1]).endOf("day") : null;
    const query = keyword.trim().toLowerCase();

    return (contracts || []).filter((contract) => {
      if (selectedUserId && contract?.userId !== selectedUserId) return false;

      const meta = statusMeta(contract);
      if (statusFilter !== "all" && meta.value !== statusFilter) return false;

      if (from || to) {
        const start = contract?.startDate
          ? dayjs(contract.startDate).startOf("day")
          : null;
        const end = contract?.endDate
          ? dayjs(contract.endDate).endOf("day")
          : dayjs("9999-12-31");
        if (!start) return false;
        if (to && start.isAfter(to)) return false;
        if (from && end.isBefore(from)) return false;
      }

      if (!query) return true;
      const fields = [
        contract?.userName,
        contract?.baseSalary,
        contract?.startDate,
        contract?.endDate,
      ].map((v) => String(v ?? "").toLowerCase());
      return fields.some((f) => f.includes(query));
    });
  }, [contracts, keyword, selectedUserId, statusFilter, range]);

  const columns = [
    {
      title: "User",
      dataIndex: "userName",
      key: "userName",
      render: (v) => v || "-",
    },
    {
      title: "Base Salary",
      dataIndex: "baseSalary",
      key: "baseSalary",
      sorter: (a, b) => Number(a?.baseSalary || 0) - Number(b?.baseSalary || 0),
      render: (v) => currency(v),
    },
    {
      title: "Start Date",
      dataIndex: "startDate",
      key: "startDate",
      sorter: (a, b) =>
        dayjs(a?.startDate || "1970-01-01").valueOf() -
        dayjs(b?.startDate || "1970-01-01").valueOf(),
      render: (v) => v || "-",
    },
    {
      title: "End Date",
      dataIndex: "endDate",
      key: "endDate",
      sorter: (a, b) =>
        dayjs(a?.endDate || "9999-12-31").valueOf() -
        dayjs(b?.endDate || "9999-12-31").valueOf(),
      render: (v) => v || "-",
    },
    {
      title: "Status",
      key: "status",
      render: (_, r) => {
        const meta = statusMeta(r);
        return <Tag color={meta.color}>{meta.label}</Tag>;
      },
    },
    {
      title: "Action",
      key: "action",
      render: (_, r) => (
        <Button size="small" onClick={() => onEdit(r)}>
          Edit
        </Button>
      ),
    },
  ];

  return (
    <div
      className="rounded-xl p-4 space-y-3"
      style={{
        backgroundColor: "#FFFFFF",
        boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
      }}
    >
      <div className="flex items-center justify-between gap-3">
        <h3 style={{ fontSize: "16px", fontWeight: 600, color: "#0A0A0A" }}>
          Salary Contracts
        </h3>
        <Button type="primary" onClick={onCreate}>
          New Contract
        </Button>
      </div>
      <Select
        value={selectedUserId}
        onChange={onUserChange}
        allowClear
        showSearch
        optionFilterProp="label"
        placeholder="Filter by user"
        style={{ width: "100%", maxWidth: 400, marginRight: 10 }}
        options={users.map((u) => ({
          value: u.id,
          label: `${u.firstName ?? ""} ${u.lastName ?? ""}`.trim() || u.email,
        }))}
      />
      <Space wrap size={10}>
        <Input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Search salary, date..."
          style={{ width: 240 }}
          allowClear
        />
        <Select
          value={statusFilter}
          onChange={setStatusFilter}
          style={{ width: 140 }}
          options={[
            { value: "all", label: "All status" },
            { value: "active", label: "Active" },
            { value: "future", label: "Future" },
            { value: "expired", label: "Expired" },
          ]}
        />
        <DatePicker.RangePicker
          value={range}
          onChange={(value) => setRange(value)}
          allowEmpty={[true, true]}
        />
        <Button
          onClick={() => {
            setKeyword("");
            setStatusFilter("all");
            setRange(null);
          }}
        >
          Clear
        </Button>
      </Space>
      <Table
        rowKey={(r) => r.id}
        loading={loading}
        dataSource={filteredContracts}
        columns={columns}
        pagination={{ pageSize: 8 }}
        locale={{ emptyText: "No contracts match current filter." }}
      />
    </div>
  );
}

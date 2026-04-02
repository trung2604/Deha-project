import { useMemo, useState } from "react";
import { Button, DatePicker, Input, Select, Table, Tag } from "antd";
import dayjs from "dayjs";
import { FileText, Search, RotateCcw } from "lucide-react";
import { payrollPrimaryButtonStyle, payrollSmallPrimaryButtonStyle } from "../constants/buttonStyles";

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
      width: 160,
      render: (v) => <span style={{ fontWeight: "600" }}>{v || "-"}</span>,
    },
    {
      title: "Base Salary",
      dataIndex: "baseSalary",
      key: "baseSalary",
      width: 140,
      align: "right",
      sorter: (a, b) => Number(a?.baseSalary || 0) - Number(b?.baseSalary || 0),
      render: (v) => <span style={{ color: "#52c41a", fontWeight: "600" }}>{currency(v)}</span>,
    },
    {
      title: "Start Date",
      dataIndex: "startDate",
      key: "startDate",
      width: 120,
      sorter: (a, b) =>
        dayjs(a?.startDate || "1970-01-01").valueOf() -
        dayjs(b?.startDate || "1970-01-01").valueOf(),
      render: (v) => v || "-",
    },
    {
      title: "End Date",
      dataIndex: "endDate",
      key: "endDate",
      width: 120,
      sorter: (a, b) =>
        dayjs(a?.endDate || "9999-12-31").valueOf() -
        dayjs(b?.endDate || "9999-12-31").valueOf(),
      render: (v) => v || "-",
    },
    {
      title: "Status",
      key: "status",
      width: 100,
      render: (_, r) => {
        const meta = statusMeta(r);
        return <Tag color={meta.color}>{meta.label}</Tag>;
      },
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
          className="rounded-lg transition-all duration-200 hover:opacity-95"
          style={payrollSmallPrimaryButtonStyle}
          onClick={() => onEdit(r)}
        >
          Edit
        </Button>
      ),
    },
  ];

  return (
    <div className="section-card">
      {/* Header */}
      <div
        className="section-header section-contracts-header"
        style={{ borderColor: "rgba(147, 51, 234, 0.2)", background: "linear-gradient(135deg, rgba(147, 51, 234, 0.05) 0%, rgba(147, 51, 234, 0.02) 100%)" }}
      >
        <div className="section-header-icon" style={{ color: "#9333ea" }}>
          <FileText className="w-5 h-5" />
        </div>
        <h3 style={{ margin: 0, flex: 1 }}>Salary Contracts</h3>
        <button
          type="button"
          onClick={onCreate}
          className="flex items-center gap-2 px-4 h-9 rounded-xl transition-all duration-200 hover:opacity-95"
          style={payrollPrimaryButtonStyle}
        >
          <span style={{ fontSize: "14px", fontWeight: "500" }}>New Contract</span>
        </button>
      </div>

      {/* Filters */}
      <div className="section-content" style={{ paddingBottom: "16px" }}>
        <div className="mb-4">
          <label className="form-label">Filter by Employee</label>
          <Select
            value={selectedUserId}
            onChange={onUserChange}
            allowClear
            showSearch
            placeholder="All employees"
            size="large"
            style={{ width: "100%" }}
            options={users.map((u) => ({
              value: u.id,
              label: `${u.firstName ?? ""} ${u.lastName ?? ""}`.trim() || u.email,
            }))}
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <Input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search salary, date..."
            size="large"
            prefix={<Search className="w-4 h-4 text-gray-400" />}
            allowClear
          />
          <Select
            value={statusFilter}
            onChange={setStatusFilter}
            size="large"
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
            size="large"
            allowEmpty={[true, true]}
            style={{ width: "100%" }}
          />
        </div>

        <div className="mt-3">
          <Button
            type="text"
            icon={<RotateCcw className="w-4 h-4" />}
            onClick={() => {
              setKeyword("");
              setStatusFilter("all");
              setRange(null);
            }}
          >
            Clear Filters
          </Button>
        </div>
      </div>

      {/* Table */}
      <div style={{ padding: "0 20px 20px 20px" }}>
        <Table
          rowKey={(r) => r.id}
          loading={loading}
          dataSource={filteredContracts}
          columns={columns}
          pagination={{ pageSize: 8 }}
          locale={{ emptyText: "No contracts match current filter." }}
        />
      </div>
    </div>
  );
}

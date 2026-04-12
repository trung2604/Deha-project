import { useCallback, useEffect, useMemo, useState } from "react";
import { Activity, Download, Eye, Filter, RefreshCw } from "lucide-react";
import { Button, Drawer, Input, Select, Table, Tag } from "antd";
import { useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import auditLogService from "@/features/auditLogs/api/auditLogService";
import { getAuditActionLabel, getAuditStatusColor, getAuditStatusLabel } from "@/features/auditLogs/utils/auditLogDisplay";
import { getPageContent, getPageMeta, getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";

function toIsoOrUndefined(value) {
  if (!value) return undefined;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return undefined;
  return date.toISOString();
}

function fmtDate(value) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "--";
  return date.toLocaleString();
}

function statusColor(success) {
  if (success === true) return "green";
  if (success === false) return "red";
  return "default";
}

export function AuditLogsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [rows, setRows] = useState([]);
  const [meta, setMeta] = useState({ page: 0, size: 20, totalPages: 0, totalElements: 0 });
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);
  const [pendingSelectedId, setPendingSelectedId] = useState(searchParams.get("selectedId") || "");

  const [filters, setFilters] = useState({
    success: undefined,
    from: "",
    to: "",
  });

  useEffect(() => {
    setPendingSelectedId(searchParams.get("selectedId") || "");
  }, [searchParams]);

  const requestParams = useMemo(
    () => ({
      page: meta.page,
      size: meta.size,
      success: filters.success === "true" ? true : filters.success === "false" ? false : undefined,
      from: toIsoOrUndefined(filters.from),
      to: toIsoOrUndefined(filters.to),
    }),
    [filters, meta.page, meta.size],
  );

  const loadLogs = useCallback(async () => {
    setLoading(true);
    try {
      const res = await auditLogService.listAuditLogs(requestParams);
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load audit logs"));
        return;
      }
      setRows(getPageContent(res));
      setMeta((prev) => ({ ...prev, ...getPageMeta(res) }));
    } catch {
      toast.error("Failed to load audit logs");
    } finally {
      setLoading(false);
    }
  }, [requestParams]);

  useEffect(() => {
    loadLogs();
  }, [loadLogs]);

  useEffect(() => {
    if (!pendingSelectedId || loading || !rows.length) return;
    const matched = rows.find((row) => row.id === pendingSelectedId);
    if (!matched) return;
    openDetail(matched);
    setPendingSelectedId("");
    searchParams.delete("selectedId");
    setSearchParams(searchParams, { replace: true });
  }, [pendingSelectedId, loading, rows, searchParams, setSearchParams]);

  const openDetail = async (row) => {
    if (!row?.id) return;
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      const res = await auditLogService.getAuditLogById(row.id);
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load audit log detail"));
        setDetailOpen(false);
        return;
      }
      setSelectedLog(res.data || null);
    } catch {
      toast.error("Failed to load audit log detail");
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      const blob = await auditLogService.exportAuditLogsCsv({ ...requestParams, limit: 5000 });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "audit-logs.csv";
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      toast.success("CSV exported successfully");
    } catch {
      toast.error("Failed to export CSV");
    } finally {
      setExporting(false);
    }
  };

  const columns = [
    {
      title: "Time",
      dataIndex: "occurredAt",
      key: "occurredAt",
      render: (value) => <span style={{ color: "#595959", fontSize: 12 }}>{fmtDate(value)}</span>,
    },
    {
      title: "Actor",
      dataIndex: "actorEmail",
      key: "actorEmail",
      render: (value) => value || "--",
    },
    {
      title: "Action",
      key: "action",
      render: (_, row) => (
        <span style={{ fontSize: 13, fontWeight: 700, color: "#0A0A0A" }}>
          {getAuditActionLabel(row.httpMethod, row.endpointPattern)}
        </span>
      ),
    },
    {
      title: "Result",
      key: "status",
      render: (_, row) => (
        <Tag color={statusColor(row.success)}>
          {getAuditStatusLabel(row.success)}
        </Tag>
      ),
    },
    {
      title: "",
      key: "view",
      width: 72,
      render: (_, row) => (
        <Button size="small" icon={<Eye className="w-4 h-4" />} onClick={() => openDetail(row)} />
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="page-hero">
          <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <Activity className="w-5 h-5" style={{ color: "#5B7CFF" }} />
              <h1 className="page-title" style={{ fontSize: 24 }}>System Activity Logs</h1>
            </div>
              <p className="page-subtitle">Track scoped system/security actions (auth, user, org, payroll, OT, approvals) and delete events. Read-only views are not logged.</p>
          </div>
          <div className="flex items-center gap-2">
            <span className="metric-chip">{meta.totalElements} events</span>
            <Button icon={<Download className="w-4 h-4" />} onClick={handleExport} loading={exporting}>
              Export CSV
            </Button>
            <Button icon={<RefreshCw className="w-4 h-4" />} onClick={loadLogs}>
              Reload
            </Button>
          </div>
        </div>
      </div>

      <div className="glass-surface page-surface p-4 soft-ring">
          <div className="flex items-center gap-2 mb-3">
          <Filter className="w-4 h-4" style={{ color: "#5B7CFF" }} />
          <h3 style={{ margin: 0, fontSize: 14, fontWeight: 600 }}>Filters</h3>
        </div>
        <div className="grid gap-3 md:grid-cols-3">
          <Select
            allowClear
            placeholder="Result"
            value={filters.success}
            options={[
              { value: "true", label: "Success" },
              { value: "false", label: "Failed" },
            ]}
            onChange={(value) => setFilters((prev) => ({ ...prev, success: value }))}
          />
          <Input
            type="datetime-local"
            value={filters.from}
            onChange={(e) => setFilters((prev) => ({ ...prev, from: e.target.value }))}
          />
          <Input
            type="datetime-local"
            value={filters.to}
            onChange={(e) => setFilters((prev) => ({ ...prev, to: e.target.value }))}
          />
        </div>
        <div className="mt-3 flex gap-2">
          <Button
            type="primary"
            onClick={() => {
              setMeta((prev) => ({ ...prev, page: 0 }));
              const nextParams = new URLSearchParams();
              if (filters.success !== undefined && filters.success !== null && filters.success !== "") nextParams.set("success", filters.success);
              if (filters.from) nextParams.set("from", filters.from);
              if (filters.to) nextParams.set("to", filters.to);
              if (pendingSelectedId) nextParams.set("selectedId", pendingSelectedId);
              setSearchParams(nextParams, { replace: true });
            }}
          >
            Apply
          </Button>
          <Button
            onClick={() => {
              setFilters({
                success: undefined,
                from: "",
                to: "",
              });
              setPendingSelectedId("");
              setSearchParams({}, { replace: true });
              setMeta((prev) => ({ ...prev, page: 0 }));
            }}
          >
            Reset
          </Button>
        </div>
      </div>

      <div className="glass-surface page-surface p-4 soft-ring">
        <Table
          rowKey="id"
          loading={loading}
          dataSource={rows}
          columns={columns}
          pagination={{
            current: meta.page + 1,
            pageSize: meta.size,
            total: meta.totalElements,
            showSizeChanger: true,
            onChange: (nextPage, nextSize) => {
              setMeta((prev) => ({ ...prev, page: nextPage - 1, size: nextSize }));
            },
          }}
        />
      </div>

      <Drawer
        title="Audit Log Details"
        open={detailOpen}
        width={520}
        onClose={() => {
          setDetailOpen(false);
          setSelectedLog(null);
        }}
      >
        {detailLoading ? (
          <p style={{ color: "#8C8C8C" }}>Loading...</p>
        ) : !selectedLog ? (
          <p style={{ color: "#8C8C8C" }}>No data available</p>
        ) : (
          <div className="space-y-2">
            <Field label="Time" value={fmtDate(selectedLog.occurredAt)} />
            <Field label="Actor" value={selectedLog.actorEmail || "--"} />
            <Field label="Action" value={getAuditActionLabel(selectedLog.httpMethod, selectedLog.endpointPattern)} />
            <Field label="Result" value={<Tag color={getAuditStatusColor(selectedLog.success)} style={{ margin: 0 }}>{getAuditStatusLabel(selectedLog.success)}</Tag>} />
          </div>
        )}
      </Drawer>
    </div>
  );
}

function Field({ label, value, mono = false }) {
  return (
    <div className="rounded-lg p-3" style={{ border: "1px solid #E8E8E8", backgroundColor: "#fff" }}>
      <div style={{ fontSize: 12, color: "#8C8C8C", marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 13, color: "#0A0A0A", fontFamily: mono ? "JetBrains Mono, monospace" : "inherit", wordBreak: "break-word" }}>
        {value}
      </div>
    </div>
  );
}


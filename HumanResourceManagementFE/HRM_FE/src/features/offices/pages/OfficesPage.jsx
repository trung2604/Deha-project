import { useEffect, useState } from "react";
import { Edit, Plus, Trash2 } from "lucide-react";
import { Modal, Input, Form } from "antd";
import { toast } from "sonner";
import officeService from "@/features/offices/api/officeService";
import {
  getOptimisticConflictMessage,
  getResponseMessage,
  isOptimisticConflictResponse,
  isSuccessResponse,
} from "@/utils/apiResponse";

export function OfficesPage() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [offices, setOffices] = useState([]);
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({
    name: "",
    description: "",
    ipWifiText: "",
  });

  const parseIpList = (text) => {
    return String(text ?? "")
      .split(/\r?\n|,/)
      .map((s) => s.trim())
      .filter(Boolean);
  };

  const load = async () => {
    setLoading(true);
    try {
      const res = await officeService.getOffices();
      if (!isSuccessResponse(res))
        return toast.error(getResponseMessage(res, "Failed to load offices"));
      setOffices(Array.isArray(res?.data) ? res.data : []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load().catch(() => toast.error("Failed to load offices"));
  }, []);

  const openCreate = () => {
    setEditing(null);
    setForm({
      name: "",
      description: "",
      ipWifiText: "",
    });
    setOpen(true);
  };

  const openEdit = (office) => {
    setEditing(office);
    setForm({
      name: office?.name ?? "",
      description: office?.description ?? "",
      ipWifiText: Array.isArray(office?.ipWifiIps)
        ? office.ipWifiIps.join("\n")
        : "",
    });
    setOpen(true);
  };

  const save = async () => {
    if (!form.name.trim()) return toast.error("Office name is required");
    if (editing?.id && editing?.version == null) {
      return toast.error(getOptimisticConflictMessage());
    }
    const ipWifiIps = parseIpList(form.ipWifiText);
    if (ipWifiIps.length === 0)
      return toast.error("Office must have at least 1 WiFi IP");

    setSaving(true);
    try {
      const payload = {
        name: form.name.trim(),
        description: form.description?.trim() || null,
        ipWifiIps,
        ...(editing?.id ? { expectedVersion: editing.version } : {}),
      };
      const res = editing?.id
        ? await officeService.updateOffice(editing.id, payload)
        : await officeService.createOffice(payload);
      if (!isSuccessResponse(res)) {
        return toast.error(
          isOptimisticConflictResponse(res)
            ? getOptimisticConflictMessage(res)
            : getResponseMessage(res, "Failed to save office"),
        );
      }
      toast.success(
        getResponseMessage(
          res,
          editing
            ? "Office updated successfully"
            : "Office created successfully",
        ),
      );
      setOpen(false);
      setEditing(null);
      await load();
    } finally {
      setSaving(false);
    }
  };

  const remove = async (office) => {
    if (!office?.id) return;
    setSaving(true);
    try {
      const res = await officeService.deleteOffice(office.id);
      if (!isSuccessResponse(res))
        return toast.error(getResponseMessage(res, "Failed to delete office"));
      toast.success(getResponseMessage(res, "Office deleted successfully"));
      await load();
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="page-hero">
        <div className="flex items-center justify-between gap-4 flex-wrap">
          <div className="flex items-center gap-3">
            <h1 className="page-title">Offices</h1>
            <span className="metric-chip">{offices.length}</span>
          </div>
          <button onClick={openCreate} className="btn-primary-gradient">
            <Plus className="w-4 h-4" />
            Add Office
          </button>
        </div>
        <p className="page-subtitle">
          Configure office profiles and allowed WiFi networks for attendance and OT access control.
        </p>
      </div>

      <div
        className="rounded-xl overflow-hidden glass-surface page-surface"
        style={{
          boxShadow: "0 10px 26px rgba(35,57,110,0.1)",
        }}
      >
        {loading ? (
          <div className="p-6" style={{ color: "#8C8C8C" }}>
            Loading offices…
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px]">
            <thead style={{ backgroundColor: "#F5F7FA" }}>
              <tr>
                <th
                  className="px-4 py-3 text-left"
                  style={{ fontSize: 12, color: "#595959" }}
                >
                  Name
                </th>
                <th
                  className="px-4 py-3 text-left"
                  style={{ fontSize: 12, color: "#595959" }}
                >
                  Description
                </th>
                <th
                  className="px-4 py-3 text-right"
                  style={{ fontSize: 12, color: "#595959" }}
                >
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {offices.map((office) => (
                <tr
                  key={office.id}
                  className="border-t"
                  style={{ borderColor: "#E8E8E8" }}
                >
                  <td
                    className="px-4 py-3"
                    style={{ color: "#0A0A0A", fontWeight: 600 }}
                  >
                    {office.name}
                  </td>
                  <td className="px-4 py-3" style={{ color: "#595959" }}>
                    {office.description || "—"}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        className="p-2 rounded hover:bg-blue-50"
                        onClick={() => openEdit(office)}
                      >
                        <Edit
                          className="w-4 h-4"
                          style={{ color: "#1677FF" }}
                        />
                      </button>
                      <button
                        className="p-2 rounded hover:bg-red-50"
                        onClick={() => remove(office)}
                        disabled={saving}
                      >
                        <Trash2
                          className="w-4 h-4"
                          style={{ color: "#FF4D4F" }}
                        />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {offices.length === 0 && (
                <tr>
                  <td
                    className="px-4 py-10 text-center"
                    colSpan={3}
                    style={{ color: "#8C8C8C" }}
                  >
                    No offices yet
                  </td>
                </tr>
              )}
            </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal
        title={editing ? "Edit Office" : "Add Office"}
        open={open}
        onCancel={() => setOpen(false)}
        onOk={save}
        okText={editing ? "Update" : "Create"}
        confirmLoading={saving}
      >
        <Form layout="vertical">
          <Form.Item label="Office name" required>
            <Input
              value={form.name}
              onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
              placeholder="Office name"
            />
          </Form.Item>

          <Form.Item label="Description">
            <Input.TextArea
              value={form.description}
              onChange={(e) =>
                setForm((p) => ({ ...p, description: e.target.value }))
              }
              placeholder="Description"
              rows={3}
            />
          </Form.Item>

          <Form.Item label="WiFi IP list" required>
            <Input.TextArea
              value={form.ipWifiText}
              onChange={(e) =>
                setForm((p) => ({ ...p, ipWifiText: e.target.value }))
              }
              placeholder={
                "WiFi IP list (mỗi dòng 1 IP)\nVí dụ:\n192.168.1.1\n192.168.1.2"
              }
              rows={4}
            />
          </Form.Item>

        </Form>
      </Modal>
    </div>
  );
}

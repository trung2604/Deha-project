import { useEffect, useState } from "react";
import { Edit, Plus, Trash2 } from "lucide-react";
import { Modal, Input, Form, InputNumber } from "antd";
import { toast } from "sonner";
import officeService from "@/features/offices/api/officeService";
import { getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";

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
    baseWorkHoursPerDay: 9,
    otWeekdayMultiplier: 1.5,
    otWeekendMultiplier: 2.0,
    otHolidayMultiplier: 3.0,
    otNightBonusMultiplier: 0.3,
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
      baseWorkHoursPerDay: 9,
      otWeekdayMultiplier: 1.5,
      otWeekendMultiplier: 2.0,
      otHolidayMultiplier: 3.0,
      otNightBonusMultiplier: 0.3,
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
      baseWorkHoursPerDay: office?.baseWorkHoursPerDay ?? 9,
      otWeekdayMultiplier: office?.otWeekdayMultiplier ?? 1.5,
      otWeekendMultiplier: office?.otWeekendMultiplier ?? 2.0,
      otHolidayMultiplier: office?.otHolidayMultiplier ?? 3.0,
      otNightBonusMultiplier: office?.otNightBonusMultiplier ?? 0.3,
    });
    setOpen(true);
  };

  const save = async () => {
    if (!form.name.trim()) return toast.error("Office name is required");
    const ipWifiIps = parseIpList(form.ipWifiText);
    if (ipWifiIps.length === 0)
      return toast.error("Office must have at least 1 WiFi IP");
    if (!form.baseWorkHoursPerDay || form.baseWorkHoursPerDay <= 0)
      return toast.error("Base work hours per day must be greater than 0");

    setSaving(true);
    try {
      const payload = {
        name: form.name.trim(),
        description: form.description?.trim() || null,
        ipWifiIps,
        baseWorkHoursPerDay: Number(form.baseWorkHoursPerDay),
        otWeekdayMultiplier: Number(form.otWeekdayMultiplier),
        otWeekendMultiplier: Number(form.otWeekendMultiplier),
        otHolidayMultiplier: Number(form.otHolidayMultiplier),
        otNightBonusMultiplier: Number(form.otNightBonusMultiplier),
      };
      const res = editing?.id
        ? await officeService.updateOffice(editing.id, payload)
        : await officeService.createOffice(payload);
      if (!isSuccessResponse(res))
        return toast.error(getResponseMessage(res, "Failed to save office"));
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
      <div className="flex items-center justify-between">
        <h1
          style={{
            fontFamily: "DM Sans, sans-serif",
            fontSize: 24,
            fontWeight: 600,
            color: "#0A0A0A",
          }}
        >
          Offices
        </h1>
        <button
          onClick={openCreate}
          className="flex items-center gap-2 px-4 h-9 rounded-xl transition-all duration-200 hover:opacity-95"
          style={{
            background: "linear-gradient(135deg, #1677FF 0%, #0958D9 100%)",
            color: "#FFFFFF",
            boxShadow: "0 8px 20px rgba(22,119,255,0.26)",
          }}
        >
          <Plus className="w-4 h-4" />
          Add Office
        </button>
      </div>

      <div
        className="rounded-xl overflow-hidden"
        style={{
          backgroundColor: "#FFFFFF",
          boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
        }}
      >
        {loading ? (
          <div className="p-6" style={{ color: "#8C8C8C" }}>
            Loading offices…
          </div>
        ) : (
          <table className="w-full">
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

          <Form.Item label="Base work hours per day" required>
            <InputNumber
              value={form.baseWorkHoursPerDay}
              onChange={(value) =>
                setForm((p) => ({ ...p, baseWorkHoursPerDay: value ?? 0 }))
              }
              min={1}
              style={{ width: "100%" }}
            />
          </Form.Item>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <Form.Item label="OT weekday multiplier" required>
              <InputNumber
                value={form.otWeekdayMultiplier}
                onChange={(value) =>
                  setForm((p) => ({ ...p, otWeekdayMultiplier: value ?? 0 }))
                }
                min={0.1}
                step={0.1}
                style={{ width: "100%" }}
              />
            </Form.Item>
            <Form.Item label="OT weekend multiplier" required>
              <InputNumber
                value={form.otWeekendMultiplier}
                onChange={(value) =>
                  setForm((p) => ({ ...p, otWeekendMultiplier: value ?? 0 }))
                }
                min={0.1}
                step={0.1}
                style={{ width: "100%" }}
              />
            </Form.Item>
            <Form.Item label="OT holiday multiplier" required>
              <InputNumber
                value={form.otHolidayMultiplier}
                onChange={(value) =>
                  setForm((p) => ({ ...p, otHolidayMultiplier: value ?? 0 }))
                }
                min={0.1}
                step={0.1}
                style={{ width: "100%" }}
              />
            </Form.Item>
            <Form.Item label="OT night bonus multiplier" required>
              <InputNumber
                value={form.otNightBonusMultiplier}
                onChange={(value) =>
                  setForm((p) => ({ ...p, otNightBonusMultiplier: value ?? 0 }))
                }
                min={0}
                step={0.1}
                style={{ width: "100%" }}
              />
            </Form.Item>
          </div>
        </Form>
      </Modal>
    </div>
  );
}

import { useEffect, useState } from "react";
import { Button, Form, Input, InputNumber } from "antd";
import { toast } from "sonner";
import officeService from "@/features/offices/api/officeService";
import {
  getOptimisticConflictMessage,
  getResponseMessage,
  isOptimisticConflictResponse,
  isSuccessResponse,
} from "@/utils/apiResponse";

const normalizeTimeForInput = (value, fallback = "22:00") => {
  if (!value) return fallback;
  const text = String(value).trim();
  return text.length >= 5 ? text.slice(0, 5) : fallback;
};

export function OfficePolicyPage() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    version: null,
    officeName: "",
    baseWorkHoursPerDay: 9,
    otMinHours: 1,
    latestCheckoutTime: "22:00",
    nightStartTime: "22:00",
    nightEndTime: "06:00",
    otWeekdayMultiplier: 1.5,
    otWeekendMultiplier: 2.0,
    otHolidayMultiplier: 3.0,
    otNightBonusMultiplier: 0.3,
  });

  const loadPolicy = async () => {
    setLoading(true);
    try {
      const res = await officeService.getMyOfficePolicy();
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Failed to load office policy"));
      }
      const data = res?.data ?? {};
      setForm({
        version: data?.version ?? null,
        officeName: data?.officeName ?? "",
        baseWorkHoursPerDay: data?.baseWorkHoursPerDay ?? 9,
        otMinHours: data?.otMinHours ?? 1,
        latestCheckoutTime: normalizeTimeForInput(data?.latestCheckoutTime),
        nightStartTime: normalizeTimeForInput(data?.nightStartTime, "22:00"),
        nightEndTime: normalizeTimeForInput(data?.nightEndTime, "06:00"),
        otWeekdayMultiplier: data?.otWeekdayMultiplier ?? 1.5,
        otWeekendMultiplier: data?.otWeekendMultiplier ?? 2.0,
        otHolidayMultiplier: data?.otHolidayMultiplier ?? 3.0,
        otNightBonusMultiplier: data?.otNightBonusMultiplier ?? 0.3,
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPolicy().catch(() => toast.error("Failed to load office policy"));
  }, []);

  const savePolicy = async () => {
    if (form.version == null) {
      toast.error(getOptimisticConflictMessage());
      return;
    }
    setSaving(true);
    try {
      const payload = {
        expectedVersion: form.version,
        baseWorkHoursPerDay: Number(form.baseWorkHoursPerDay),
        otMinHours: Number(form.otMinHours),
        latestCheckoutTime: normalizeTimeForInput(form.latestCheckoutTime),
        nightStartTime: normalizeTimeForInput(form.nightStartTime, "22:00"),
        nightEndTime: normalizeTimeForInput(form.nightEndTime, "06:00"),
        otWeekdayMultiplier: Number(form.otWeekdayMultiplier),
        otWeekendMultiplier: Number(form.otWeekendMultiplier),
        otHolidayMultiplier: Number(form.otHolidayMultiplier),
        otNightBonusMultiplier: Number(form.otNightBonusMultiplier),
      };
      const res = await officeService.updateMyOfficePolicy(payload);
      if (!isSuccessResponse(res)) {
        return toast.error(
          isOptimisticConflictResponse(res)
            ? getOptimisticConflictMessage(res)
            : getResponseMessage(res, "Failed to update office policy"),
        );
      }
      toast.success(getResponseMessage(res, "Office policy updated successfully"));
      await loadPolicy();
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <h1 style={{ fontSize: 24, fontWeight: 600, color: "#0A0A0A" }}>Office Policy</h1>

      <div
        className="rounded-xl p-5"
        style={{ backgroundColor: "#FFFFFF", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
      >
        {loading ? (
          <p style={{ color: "#8C8C8C" }}>Loading office policy...</p>
        ) : (
          <Form layout="vertical">
            <Form.Item label="Office">
              <Input value={form.officeName} disabled />
            </Form.Item>
            <Form.Item label="Base work hours per day" required>
              <InputNumber
                min={1}
                style={{ width: "100%" }}
                value={form.baseWorkHoursPerDay}
                onChange={(v) => setForm((p) => ({ ...p, baseWorkHoursPerDay: v ?? 1 }))}
              />
            </Form.Item>
            <Form.Item label="OT minimum hours" required>
              <InputNumber
                min={1}
                style={{ width: "100%" }}
                value={form.otMinHours}
                onChange={(v) => setForm((p) => ({ ...p, otMinHours: v ?? 1 }))}
              />
            </Form.Item>
            <Form.Item label="Latest checkout time" required>
              <Input
                type="time"
                value={form.latestCheckoutTime}
                onChange={(e) => setForm((p) => ({ ...p, latestCheckoutTime: e.target.value }))}
              />
            </Form.Item>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <Form.Item label="Night OT start time" required>
                <Input
                  type="time"
                  value={form.nightStartTime}
                  onChange={(e) => setForm((p) => ({ ...p, nightStartTime: e.target.value }))}
                />
              </Form.Item>
              <Form.Item label="Night OT end time" required>
                <Input
                  type="time"
                  value={form.nightEndTime}
                  onChange={(e) => setForm((p) => ({ ...p, nightEndTime: e.target.value }))}
                />
              </Form.Item>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <Form.Item label="OT weekday multiplier" required>
                <InputNumber
                  min={0.1}
                  step={0.1}
                  style={{ width: "100%" }}
                  value={form.otWeekdayMultiplier}
                  onChange={(v) => setForm((p) => ({ ...p, otWeekdayMultiplier: v ?? 0.1 }))}
                />
              </Form.Item>
              <Form.Item label="OT weekend multiplier" required>
                <InputNumber
                  min={0.1}
                  step={0.1}
                  style={{ width: "100%" }}
                  value={form.otWeekendMultiplier}
                  onChange={(v) => setForm((p) => ({ ...p, otWeekendMultiplier: v ?? 0.1 }))}
                />
              </Form.Item>
              <Form.Item label="OT holiday multiplier" required>
                <InputNumber
                  min={0.1}
                  step={0.1}
                  style={{ width: "100%" }}
                  value={form.otHolidayMultiplier}
                  onChange={(v) => setForm((p) => ({ ...p, otHolidayMultiplier: v ?? 0.1 }))}
                />
              </Form.Item>
              <Form.Item label="OT night bonus multiplier" required>
                <InputNumber
                  min={0}
                  step={0.1}
                  style={{ width: "100%" }}
                  value={form.otNightBonusMultiplier}
                  onChange={(v) => setForm((p) => ({ ...p, otNightBonusMultiplier: v ?? 0 }))}
                />
              </Form.Item>
            </div>
            <Button type="primary" loading={saving} onClick={savePolicy}>
              Save Policy
            </Button>
          </Form>
        )}
      </div>
    </div>
  );
}

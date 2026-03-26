import { Button, Select } from "antd";

export function PayrollFilters({
  year,
  month,
  officeId,
  offices,
  showOfficeFilter,
  onYearChange,
  onMonthChange,
  onOfficeChange,
  onReload,
}) {
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
      <div className="flex flex-wrap items-center gap-3">
        <Select
          value={year}
          options={yearOptions}
          style={{ minWidth: 140 }}
          onChange={onYearChange}
        />
        <Select
          value={month}
          options={monthOptions}
          style={{ minWidth: 140 }}
          onChange={onMonthChange}
        />
        {showOfficeFilter && (
          <Select
            value={officeId}
            allowClear
            placeholder="All offices"
            style={{ minWidth: 220 }}
            options={offices.map((o) => ({ value: o.id, label: o.name }))}
            onChange={onOfficeChange}
          />
        )}
        <Button onClick={onReload}>Reload</Button>
      </div>
    </div>
  );
}


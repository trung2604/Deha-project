import { Button, Select } from "antd";
import { Filter, RotateCcw } from "lucide-react";
import { payrollPrimaryButtonStyle } from "../constants/buttonStyles";

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
    <div className="section-card mb-6">
      <div
        className="section-header section-payroll-header"
        style={{ borderColor: "rgba(82, 196, 26, 0.2)" }}
      >
        <div className="section-header-icon">
          <Filter className="w-5 h-5" style={{ color: "#52c41a" }} />
        </div>
        <h3 style={{ margin: 0 }}>Filters</h3>
      </div>
      <div className="section-content">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          <div>
            <label className="form-label">Year</label>
            <Select
              value={year}
              options={yearOptions}
              onChange={onYearChange}
              style={{ width: "100%" }}
              size="large"
            />
          </div>
          <div>
            <label className="form-label">Month</label>
            <Select
              value={month}
              options={monthOptions}
              onChange={onMonthChange}
              style={{ width: "100%" }}
              size="large"
            />
          </div>
          {showOfficeFilter && (
            <div>
              <label className="form-label">Office</label>
              <Select
                value={officeId}
                allowClear
                placeholder="All offices"
                style={{ width: "100%" }}
                size="large"
                options={offices.map((o) => ({ value: o.id, label: o.name }))}
                onChange={onOfficeChange}
              />
            </div>
          )}
          <div className={`flex items-end ${showOfficeFilter ? "col-span-1" : "col-span-2"}`}>
            <Button
              type="primary"
              icon={<RotateCcw className="w-4 h-4" />}
              onClick={onReload}
              size="large"
              className="rounded-xl transition-all duration-200 hover:opacity-95"
              style={{ width: "100%", ...payrollPrimaryButtonStyle }}
            >
              Reload
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}


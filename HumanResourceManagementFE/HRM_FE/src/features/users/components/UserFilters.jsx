import { Search } from 'lucide-react';
import { Select, Input } from 'antd';

export function UserFilters({
  searchTerm,
  onSearchTermChange,
  departmentFilter,
  onDepartmentFilterChange,
  positionFilter,
  onPositionFilterChange,
  statusFilter,
  onStatusFilterChange,
  departments,
  positions,
  onReset,
}) {
  return (
    <div
      className="rounded-xl p-4"
      style={{ backgroundColor: '#FFFFFF', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}
    >
      <div className="flex flex-wrap items-center gap-3">
        <div className="flex-1 min-w-[200px] relative">
          <Input
            placeholder="Search users..."
            value={searchTerm}
            onChange={(e) => onSearchTermChange(e.target.value)}
            prefix={<Search className="w-4 h-4" style={{ color: '#595959' }} />}
            style={{ width: '100%' }}
            size="middle"
          />
        </div>

        <Select
          value={departmentFilter}
          onChange={(value) => onDepartmentFilterChange(value ?? '')}
          style={{ minWidth: '140px' }}
          placeholder="All Departments"
          size="middle"
          options={[
            { value: '', label: 'All Departments' },
            ...departments.map((dept) => ({ value: dept, label: dept })),
          ]}
        />

        <Select
          value={positionFilter}
          onChange={(value) => onPositionFilterChange(value ?? '')}
          style={{ minWidth: '140px' }}
          placeholder="All Positions"
          size="middle"
          options={[
            { value: '', label: 'All Positions' },
            ...positions.map((pos) => ({ value: pos, label: pos })),
          ]}
        />

        <Select
          value={statusFilter}
          onChange={(value) => onStatusFilterChange(value ?? '')}
          style={{ minWidth: '120px' }}
          placeholder="All Status"
          size="middle"
          options={[
            { value: '', label: 'All Status' },
            { value: 'Active', label: 'Active' },
            { value: 'Inactive', label: 'Inactive' },
            { value: 'On Leave', label: 'On Leave' },
          ]}
        />

        <button
          onClick={onReset}
          className="px-3 h-9 transition-colors duration-150 hover:underline"
          style={{ color: '#1677FF', fontSize: '14px', fontWeight: '500' }}
        >
          Reset
        </button>
      </div>
    </div>
  );
}


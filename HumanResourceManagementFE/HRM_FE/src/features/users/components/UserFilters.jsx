import { Search } from 'lucide-react';
import { Select, Input, Button, Spin } from 'antd';

export function UserFilters({
  searchTerm,
  onSearchTermChange,
  isSearchPending = false,
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
        <div className={`flex-1 min-w-[200px] relative ${isSearchPending ? 'search-input-pending' : ''}`}>
          <Input
            placeholder="Search users..."
            value={searchTerm}
            onChange={(e) => onSearchTermChange(e.target.value)}
            prefix={<Search className="w-4 h-4" style={{ color: '#595959' }} />}
            suffix={
              isSearchPending ? (
                <Spin size="small" style={{ color: '#1677FF' }} />
              ) : null
            }
            allowClear
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
          allowClear
          options={[
            { value: '', label: 'All Departments' },
            ...departments.map((dept) => ({ value: dept.id, label: dept.name })),
          ]}
        />

        <Select
          value={positionFilter}
          onChange={(value) => onPositionFilterChange(value ?? '')}
          style={{ minWidth: '140px' }}
          placeholder="All Positions"
          size="middle"
          allowClear
          options={[
            { value: '', label: 'All Positions' },
            ...positions.map((pos) => ({ value: pos.id, label: pos.name })),
          ]}
        />

        <Select
          value={statusFilter}
          onChange={(value) => onStatusFilterChange(value ?? '')}
          style={{ minWidth: '120px' }}
          placeholder="All Status"
          size="middle"
          allowClear
          options={[
            { value: '', label: 'All Status' },
            { value: 'true', label: 'Active' },
            { value: 'false', label: 'Inactive' },
          ]}
        />

        <Button
          onClick={onReset}
          type="text"
          size="middle"
          style={{ color: '#1677FF', fontSize: '14px', fontWeight: '500', paddingInline: 8 }}
        >
          Reset
        </Button>
      </div>
    </div>
  );
}


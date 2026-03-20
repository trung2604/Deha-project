import { Search } from 'lucide-react';

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
          <Search
            className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4"
            style={{ color: '#595959' }}
          />
          <input
            type="text"
            placeholder="Search users..."
            value={searchTerm}
            onChange={(e) => onSearchTermChange(e.target.value)}
            className="w-full h-9 pl-10 pr-4 rounded-lg border outline-none transition-all duration-150"
            style={{
              borderColor: '#E8E8E8',
              color: '#0A0A0A',
            }}
          />
        </div>

        <select
          value={departmentFilter}
          onChange={(e) => onDepartmentFilterChange(e.target.value)}
          className="h-9 px-3 rounded-lg border outline-none transition-all duration-150 cursor-pointer"
          style={{
            borderColor: '#E8E8E8',
            color: '#0A0A0A',
            minWidth: '140px',
          }}
        >
          <option value="">All Departments</option>
          {departments.map((dept) => (
            <option key={dept} value={dept}>
              {dept}
            </option>
          ))}
        </select>

        <select
          value={positionFilter}
          onChange={(e) => onPositionFilterChange(e.target.value)}
          className="h-9 px-3 rounded-lg border outline-none transition-all duration-150 cursor-pointer"
          style={{
            borderColor: '#E8E8E8',
            color: '#0A0A0A',
            minWidth: '140px',
          }}
        >
          <option value="">All Positions</option>
          {positions.map((pos) => (
            <option key={pos} value={pos}>
              {pos}
            </option>
          ))}
        </select>

        <select
          value={statusFilter}
          onChange={(e) => onStatusFilterChange(e.target.value)}
          className="h-9 px-3 rounded-lg border outline-none transition-all duration-150 cursor-pointer"
          style={{
            borderColor: '#E8E8E8',
            color: '#0A0A0A',
            minWidth: '120px',
          }}
        >
          <option value="">All Status</option>
          <option value="Active">Active</option>
          <option value="Inactive">Inactive</option>
          <option value="On Leave">On Leave</option>
        </select>

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


import { Input } from "antd";

function ReadOnlyField({ label, value }) {
  return (
    <div>
      <label className="block mb-2" style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: "500" }}>
        {label}
      </label>
      <Input
        value={value || "N/A"}
        readOnly
        style={{ borderColor: "#E8E8E8", backgroundColor: "#F5F7FA", color: "#595959" }}
        size="middle"
      />
    </div>
  );
}

export function ProfilePersonalTab({ user }) {
  return (
    <div>
      <div className="mb-6">
        <h3 className="text-lg font-semibold" style={{ color: "#0A0A0A" }}>
          Personal Information
        </h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <ReadOnlyField label="First Name" value={user?.firstName} />
        <ReadOnlyField label="Last Name" value={user?.lastName} />
        <ReadOnlyField label="Email" value={user?.email} />
        <ReadOnlyField label="Role" value={user?.role} />
      </div>

      <div className="mt-6 pt-6" style={{ borderTop: "1px solid #E8E8E8" }}>
        <h4 className="font-semibold mb-4" style={{ color: "#0A0A0A", fontSize: "14px" }}>
          Work Information
        </h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <ReadOnlyField label="User ID" value={user?.id} />
          <ReadOnlyField label="Department" value={user?.departmentName} />
          <ReadOnlyField label="Position" value={user?.positionName} />
          <ReadOnlyField label="Status" value={user?.active ? "Active" : "Inactive"} />
        </div>
      </div>
    </div>
  );
}

import { Briefcase, Calendar, Mail, Phone } from "lucide-react";

function formatDate(value) {
  if (!value) return "N/A";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return "N/A";
  return parsed.toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export function ProfileSummaryCard({ user }) {
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(" ") || "User";
  const initials = ((user?.firstName?.[0] || "") + (user?.lastName?.[0] || "") || "U").toUpperCase();
  const status = user?.active ? "Active" : "Inactive";

  const rows = [
    { icon: Mail, label: "Email", value: user?.email || "N/A" },
    { icon: Phone, label: "Phone", value: user?.phone || "N/A" },
    { icon: Briefcase, label: "Department", value: user?.departmentName || "N/A" },
    { icon: Calendar, label: "Join Date", value: formatDate(user?.joinDate) },
  ];

  return (
    <div className="rounded-lg p-6" style={{ backgroundColor: "#FFFFFF", border: "1px solid #E8E8E8" }}>
      <div className="flex flex-col items-center mb-6">
        <div
          className="w-32 h-32 rounded-full flex items-center justify-center text-white text-3xl font-bold mb-4"
          style={{ backgroundColor: "#1677FF" }}
        >
          {initials}
        </div>
        <h2 className="text-xl font-bold mb-1" style={{ color: "#0A0A0A" }}>
          {fullName}
        </h2>
        <p className="mb-4" style={{ color: "#595959", fontSize: "14px" }}>
          {user?.positionName || user?.role || "N/A"}
        </p>
        <div
          className="px-3 py-1 rounded-full text-xs font-medium"
          style={{
            backgroundColor: user?.active ? "rgba(82, 196, 26, 0.1)" : "rgba(255, 77, 79, 0.1)",
            color: user?.active ? "#52C41A" : "#FF4D4F",
          }}
        >
          {status}
        </div>
      </div>

      <div className="space-y-4 pt-4" style={{ borderTop: "1px solid #E8E8E8" }}>
        {rows.map(({ icon: Icon, label, value }) => (
          <div key={label} className="flex items-start gap-3">
            <Icon className="w-4 h-4 mt-0.5" style={{ color: "#595959" }} />
            <div className="flex-1">
              <p style={{ color: "#8C8C8C", fontSize: "12px" }}>{label}</p>
              <p style={{ color: "#0A0A0A", fontSize: "14px" }}>{value}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

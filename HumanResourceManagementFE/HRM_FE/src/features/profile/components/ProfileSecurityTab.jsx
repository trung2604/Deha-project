import { Lock } from "lucide-react";
import { toast } from "sonner";

export function ProfileSecurityTab() {
  return (
    <div>
      <h3 className="text-lg font-semibold mb-4" style={{ color: "#0A0A0A" }}>
        Security
      </h3>
      <p className="mb-6" style={{ color: "#595959", fontSize: "14px" }}>
        Password update API is not available yet in backend. This section will be enabled when endpoint is ready.
      </p>
      <button
        type="button"
        onClick={() => toast.info("Password change is not available yet")}
        className="px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-2"
        style={{ backgroundColor: "#1677FF", color: "#FFFFFF", fontSize: "14px" }}
      >
        <Lock className="w-4 h-4" />
        Change Password
      </button>
    </div>
  );
}

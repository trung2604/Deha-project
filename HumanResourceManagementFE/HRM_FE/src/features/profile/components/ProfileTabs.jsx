import { Lock, User } from "lucide-react";

export function ProfileTabs({ activeTab, onTabChange }) {
  return (
    <div className="flex border-b" style={{ borderColor: "#E8E8E8" }}>
      <button
        onClick={() => onTabChange("personal")}
        className="flex-1 px-6 py-4 font-medium transition-colors relative"
        style={{ color: activeTab === "personal" ? "#1677FF" : "#595959", fontSize: "14px" }}
      >
        <User className="w-4 h-4 inline-block mr-2" />
        Personal Information
        {activeTab === "personal" && (
          <div className="absolute bottom-0 left-0 right-0 h-0.5" style={{ backgroundColor: "#1677FF" }} />
        )}
      </button>

      <button
        onClick={() => onTabChange("security")}
        className="flex-1 px-6 py-4 font-medium transition-colors relative"
        style={{ color: activeTab === "security" ? "#1677FF" : "#595959", fontSize: "14px" }}
      >
        <Lock className="w-4 h-4 inline-block mr-2" />
        Security
        {activeTab === "security" && (
          <div className="absolute bottom-0 left-0 right-0 h-0.5" style={{ backgroundColor: "#1677FF" }} />
        )}
      </button>
    </div>
  );
}

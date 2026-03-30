import { useState } from "react";
import { toast } from "sonner";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getOptimisticConflictMessage } from "@/utils/apiResponse";
import { ProfileHeader } from "../components/ProfileHeader";
import { ProfilePersonalTab } from "../components/ProfilePersonalTab";
import { ProfileSecurityTab } from "../components/ProfileSecurityTab";
import { ProfileSummaryCard } from "../components/ProfileSummaryCard";
import { ProfileTabs } from "../components/ProfileTabs";

export default function Profile() {
  const [activeTab, setActiveTab] = useState("personal");
  const [savingProfile, setSavingProfile] = useState(false);
  const { user, updateProfile } = useAuth();

  const handleSaveProfile = async (payload) => {
    if (payload?.expectedVersion == null) {
      toast.error(getOptimisticConflictMessage());
      return false;
    }
    setSavingProfile(true);
    try {
      const res = await updateProfile(payload);
      if (!res.ok) {
        toast.error(res.message || "Failed to update profile");
        return false;
      }
      toast.success(res.message || "Profile updated successfully");
      return true;
    } finally {
      setSavingProfile(false);
    }
  };

  return (
    <div className="p-6">
      <ProfileHeader />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <ProfileSummaryCard user={user} />
        </div>

        <div className="lg:col-span-2">
          <div className="rounded-lg overflow-hidden" style={{ backgroundColor: "#FFFFFF", border: "1px solid #E8E8E8" }}>
            <ProfileTabs activeTab={activeTab} onTabChange={setActiveTab} />

            <div className="p-6">
              {activeTab === "personal" ? (
                <ProfilePersonalTab user={user} onSave={handleSaveProfile} saving={savingProfile} />
              ) : (
                <ProfileSecurityTab />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

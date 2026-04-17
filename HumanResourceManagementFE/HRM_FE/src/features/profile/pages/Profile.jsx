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
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  const { user, updateProfile, uploadAvatar, removeAvatar } = useAuth();

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

  const handleUploadAvatar = async (file) => {
    if (!file) return false;
    if (!file.type?.startsWith("image/")) {
      toast.error("Only image files are allowed");
      return false;
    }
    const maxSize = 2 * 1024 * 1024;
    if (file.size > maxSize) {
      toast.error("Avatar file must be <= 2MB");
      return false;
    }

    setUploadingAvatar(true);
    try {
      const res = await uploadAvatar(file);
      if (!res.ok) {
        toast.error(res.message || "Failed to upload avatar");
        return false;
      }
      toast.success(res.message || "Avatar uploaded successfully");
      return true;
    } finally {
      setUploadingAvatar(false);
    }
  };

  const handleRemoveAvatar = async () => {
    setUploadingAvatar(true);
    try {
      const res = await removeAvatar();
      if (!res.ok) {
        toast.error(res.message || "Failed to remove avatar");
        return false;
      }
      toast.success(res.message || "Avatar removed successfully");
      return true;
    } finally {
      setUploadingAvatar(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="page-hero">
        <ProfileHeader />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <div className="glass-surface page-surface p-1">
            <ProfileSummaryCard
              user={user}
              onAvatarUpload={handleUploadAvatar}
              onAvatarRemove={handleRemoveAvatar}
              uploadingAvatar={uploadingAvatar}
            />
          </div>
        </div>

        <div className="lg:col-span-2">
          <div className="rounded-lg overflow-hidden glass-surface page-surface" style={{ border: "1px solid #E8E8E8" }}>
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

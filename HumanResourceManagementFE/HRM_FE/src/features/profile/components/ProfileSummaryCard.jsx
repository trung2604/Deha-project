import { Briefcase, Calendar, Camera, Mail, Phone, Trash2, Upload, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { toast } from "sonner";

async function cropImageToSquare(file) {
  const imageUrl = URL.createObjectURL(file);
  try {
    const img = await new Promise((resolve, reject) => {
      const image = new Image();
      image.onload = () => resolve(image);
      image.onerror = () => reject(new Error("Invalid image file"));
      image.src = imageUrl;
    });

    const size = Math.min(img.width, img.height);
    const sx = Math.floor((img.width - size) / 2);
    const sy = Math.floor((img.height - size) / 2);

    const canvas = document.createElement("canvas");
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext("2d");
    if (!ctx) {
      throw new Error("Canvas is not supported");
    }
    ctx.drawImage(img, sx, sy, size, size, 0, 0, size, size);

    const blob = await new Promise((resolve, reject) => {
      canvas.toBlob((result) => {
        if (!result) {
          reject(new Error("Unable to crop image"));
          return;
        }
        resolve(result);
      }, file.type || "image/jpeg", 0.92);
    });

    return new File([blob], file.name, { type: blob.type || file.type || "image/jpeg" });
  } finally {
    URL.revokeObjectURL(imageUrl);
  }
}

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

export function ProfileSummaryCard({ user, onAvatarUpload, onAvatarRemove, uploadingAvatar = false }) {
  const fileInputRef = useRef(null);
  const [pendingAvatarFile, setPendingAvatarFile] = useState(null);
  const [pendingAvatarPreview, setPendingAvatarPreview] = useState("");
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(" ") || "User";
  const initials = ((user?.firstName?.[0] || "") + (user?.lastName?.[0] || "") || "U").toUpperCase();
  const status = user?.active ? "Active" : "Inactive";
  const avatarUrl = pendingAvatarPreview || user?.avatarUrl || "";
  const isPreviewMode = Boolean(pendingAvatarFile);

  useEffect(() => {
    return () => {
      if (pendingAvatarPreview) {
        URL.revokeObjectURL(pendingAvatarPreview);
      }
    };
  }, [pendingAvatarPreview]);

  const resetPendingAvatar = () => {
    setPendingAvatarFile(null);
    if (pendingAvatarPreview) {
      URL.revokeObjectURL(pendingAvatarPreview);
    }
    setPendingAvatarPreview("");
  };

  const rows = [
    { icon: Mail, label: "Email", value: user?.email || "N/A" },
    { icon: Phone, label: "Phone", value: user?.phone || "N/A" },
    { icon: Briefcase, label: "Department", value: user?.departmentName || "N/A" },
    { icon: Calendar, label: "Join Date", value: formatDate(user?.createdAt) },
  ];

  return (
    <div className="rounded-lg p-6" style={{ backgroundColor: "#FFFFFF", border: "1px solid #E8E8E8" }}>
      <div className="flex flex-col items-center mb-6">
        <div
          className="relative w-32 h-32 rounded-full flex items-center justify-center text-white text-3xl font-bold mb-4 overflow-hidden transition-all duration-300"
          style={{
            backgroundColor: "#1677FF",
            boxShadow: isPreviewMode
              ? "0 0 0 4px rgba(22,119,255,0.18), 0 10px 24px rgba(22,119,255,0.25)"
              : "0 6px 18px rgba(22,119,255,0.22)",
          }}
        >
          {avatarUrl ? (
            <img
              src={avatarUrl}
              alt={fullName}
              className="w-32 h-32 rounded-full object-cover"
            />
          ) : (
            initials
          )}
          {isPreviewMode && (
            <div
              className="absolute bottom-1 left-1/2 -translate-x-1/2 px-2 py-0.5 rounded-full text-[11px] font-semibold"
              style={{ backgroundColor: "rgba(0,0,0,0.65)", color: "#FFFFFF" }}
            >
              Preview
            </div>
          )}
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={async (e) => {
            const file = e.target.files?.[0];
            if (file) {
              try {
                const cropped = await cropImageToSquare(file);
                const previewUrl = URL.createObjectURL(cropped);
                if (pendingAvatarPreview) {
                  URL.revokeObjectURL(pendingAvatarPreview);
                }
                setPendingAvatarFile(cropped);
                setPendingAvatarPreview(previewUrl);
              } catch {
                setPendingAvatarFile(null);
                toast.error("Unable to crop selected image");
              }
            }
            e.target.value = "";
          }}
        />
        <div className="mb-3 flex flex-wrap items-center justify-center gap-2">
          <button
            type="button"
            disabled={uploadingAvatar}
            onClick={() => fileInputRef.current?.click()}
            className="px-3 py-1 rounded-lg inline-flex items-center gap-1.5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-sm active:translate-y-0 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
            style={{
              border: "1px solid #D9D9D9",
              backgroundColor: "#FFFFFF",
              color: "#0A0A0A",
              fontSize: "13px",
              fontWeight: 500,
            }}
          >
            <Camera className="w-3.5 h-3.5" />
            {pendingAvatarFile ? "Change" : "Change avatar"}
          </button>
          {pendingAvatarFile && (
            <>
              <button
                type="button"
                disabled={uploadingAvatar}
                onClick={async () => {
                  if (!onAvatarUpload) return;
                  const ok = await onAvatarUpload(pendingAvatarFile);
                  if (ok) {
                    resetPendingAvatar();
                  }
                }}
                className="px-3 py-1 rounded-lg inline-flex items-center gap-1.5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md active:translate-y-0 active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed"
                style={{
                  border: "1px solid #1677FF",
                  backgroundColor: "#1677FF",
                  color: "#FFFFFF",
                  fontSize: "13px",
                  fontWeight: 500,
                }}
              >
                <Upload className="w-3.5 h-3.5" />
                <span className={uploadingAvatar ? "animate-pulse" : ""}>
                  {uploadingAvatar ? "Uploading..." : "Upload"}
                </span>
              </button>
              <button
                type="button"
                disabled={uploadingAvatar}
                onClick={resetPendingAvatar}
                className="px-3 py-1 rounded-lg inline-flex items-center gap-1.5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-sm active:translate-y-0 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
                style={{
                  border: "1px solid #D9D9D9",
                  backgroundColor: "#FFFFFF",
                  color: "#595959",
                  fontSize: "13px",
                  fontWeight: 500,
                }}
              >
                <X className="w-3.5 h-3.5" />
                Cancel
              </button>
            </>
          )}
          {!pendingAvatarFile && user?.avatarUrl && (
            <button
              type="button"
              disabled={uploadingAvatar}
              onClick={() => onAvatarRemove && onAvatarRemove()}
              className="px-3 py-1 rounded-lg inline-flex items-center gap-1.5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-sm active:translate-y-0 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
              style={{
                border: "1px solid #FFCCC7",
                backgroundColor: "#FFF1F0",
                color: "#CF1322",
                fontSize: "13px",
                fontWeight: 500,
              }}
            >
              <Trash2 className="w-3.5 h-3.5" />
              Remove avatar
            </button>
          )}
        </div>
        <p style={{ color: "#8C8C8C", fontSize: "12px", marginTop: "-2px", marginBottom: "10px" }}>
          Supported: image files up to 2MB, cropped to a square preview.
        </p>
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
        {rows.map((row) => {
          const IconComponent = row.icon;
          return (
            <div key={row.label} className="flex items-start gap-3">
              <IconComponent className="w-4 h-4 mt-0.5" style={{ color: "#595959" }} />
              <div className="flex-1">
                <p style={{ color: "#8C8C8C", fontSize: "12px" }}>{row.label}</p>
                <p style={{ color: "#0A0A0A", fontSize: "14px" }}>{row.value}</p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

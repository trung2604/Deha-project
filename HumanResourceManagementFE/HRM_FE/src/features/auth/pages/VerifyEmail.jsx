import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button } from "antd";
import authService from "../api/authService";
import { getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";
import { AUTH_ACTION_PRIMARY_STYLE, AUTH_ACTION_SECONDARY_STYLE, AUTH_BG_STYLE, AUTH_CARD_STYLE } from "../constants/authUi";

export function VerifyEmail() {
  const VERIFY_STATUS_PREFIX = "verify_email_status:";
  const VERIFY_PROCESSING_TIMEOUT_MS = 15000;
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = useMemo(() => searchParams.get("token") || "", [searchParams]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    let mounted = true;
    let pollTimer = null;

    const applyStoredStatus = (status) => {
      if (!mounted) return;
      if (status === "success") {
        setSuccessMessage("Account activated successfully");
        setErrorMessage("");
        setLoading(false);
        return true;
      }
      if (status === "failed") {
        setErrorMessage("Verification link is invalid or has expired. The activation link is valid for 24 hours.");
        setSuccessMessage("");
        setLoading(false);
        return true;
      }
      return false;
    };

    const verify = async () => {
      if (!token) {
        if (!mounted) return;
        setErrorMessage("Missing verification token");
        setLoading(false);
        return;
      }

      const statusKey = `${VERIFY_STATUS_PREFIX}${token}`;
      const existingStatus = sessionStorage.getItem(statusKey);
      if (applyStoredStatus(existingStatus)) {
        return;
      }

      if (existingStatus === "processing") {
        const processingAtRaw = Number(sessionStorage.getItem(`${statusKey}:at`) || 0);
        const isProcessingExpired = !processingAtRaw || Date.now() - processingAtRaw > VERIFY_PROCESSING_TIMEOUT_MS;
        if (isProcessingExpired) {
          sessionStorage.removeItem(statusKey);
          sessionStorage.removeItem(`${statusKey}:at`);
        } else {
          pollTimer = window.setInterval(() => {
            const nextStatus = sessionStorage.getItem(statusKey);
            if (applyStoredStatus(nextStatus) && pollTimer) {
              window.clearInterval(pollTimer);
              pollTimer = null;
            }
          }, 150);
          return;
        }
      }

      try {
        setLoading(true);
        sessionStorage.setItem(statusKey, "processing");
        sessionStorage.setItem(`${statusKey}:at`, String(Date.now()));
        const res = await authService.verifyEmail(token);

        if (!isSuccessResponse(res)) {
          sessionStorage.setItem(statusKey, "failed");
          sessionStorage.removeItem(`${statusKey}:at`);
          if (!mounted) return;
          setErrorMessage(getResponseMessage(res, "Verification link is invalid or has expired. The activation link is valid for 24 hours."));
          setLoading(false);
          return;
        }

        sessionStorage.setItem(statusKey, "success");
        sessionStorage.removeItem(`${statusKey}:at`);
        if (!mounted) return;
        setSuccessMessage(getResponseMessage(res, "Account activated successfully"));
        setLoading(false);
      } catch {
        sessionStorage.setItem(statusKey, "failed");
        sessionStorage.removeItem(`${statusKey}:at`);
        if (!mounted) return;
        setErrorMessage("Verification link is invalid or has expired. The activation link is valid for 24 hours.");
        setLoading(false);
      }
    };

    verify();
    return () => {
      if (pollTimer) {
        window.clearInterval(pollTimer);
      }
      mounted = false;
    };
  }, [token]);

  return (
    <div className="min-h-screen flex items-center justify-center p-6" style={AUTH_BG_STYLE}>
      <div className="w-full max-w-lg rounded-2xl p-8 glass-surface page-surface" style={AUTH_CARD_STYLE}>
        <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 6, color: "#0A0A0A" }}>Email verification</h2>
        <p style={{ color: "#595959", marginBottom: 20 }}>We are validating your account activation link.</p>
        <p style={{ color: "#8C8C8C", marginBottom: 16, fontSize: 13 }}>Activation link validity: 24 hours from the time the email is sent.</p>

        {loading && <Alert type="info" showIcon description="Verifying..." />}
        {!loading && successMessage && <Alert type="success" showIcon description={successMessage} />}
        {!loading && errorMessage && <Alert type="error" showIcon description={errorMessage} />}

        <div className="mt-5 flex gap-2">
          <Button type="primary" style={AUTH_ACTION_PRIMARY_STYLE} onClick={() => navigate("/login", { replace: true })}>Back to login</Button>
          <Button style={AUTH_ACTION_SECONDARY_STYLE} onClick={() => navigate("/forgot-password")}>Forgot password</Button>
        </div>
      </div>
    </div>
  );
}


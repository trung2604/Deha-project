import { RouterProvider } from "react-router-dom";
import { router } from "./routes.jsx";
import { Toaster } from "sonner";
import { AuthProvider } from "@/features/auth/context/AuthContext";
import { ConfigProvider } from "antd";
import { NotificationProvider } from "@/features/notifications/context/NotificationContext";

export default function App() {
  return (
    <AuthProvider>
      <NotificationProvider>
        <ConfigProvider
          theme={{
            token: {
              colorPrimary: "#5B7CFF",
              colorInfo: "#5B7CFF",
              colorSuccess: "#2DBE8D",
              colorWarning: "#FFAD33",
              colorError: "#F45B69",
              borderRadius: 12,
              fontFamily: "Inter, sans-serif",
              colorBgBase: "#F4F7FF",
            },
            components: {
              Button: {
                borderRadius: 10,
                controlHeight: 38,
              },
              Input: {
                borderRadius: 10,
                controlHeight: 38,
              },
              Select: {
                borderRadius: 10,
                controlHeight: 38,
              },
              Card: {
                borderRadiusLG: 16,
              },
              Table: {
                borderRadiusLG: 14,
                headerBg: "#EEF3FF",
                headerColor: "#344054",
              },
            },
          }}
        >
          <RouterProvider router={router} />
          <Toaster
            position="top-center"
            richColors
            duration={3000}
          />
        </ConfigProvider>
      </NotificationProvider>
    </AuthProvider>
  );
}
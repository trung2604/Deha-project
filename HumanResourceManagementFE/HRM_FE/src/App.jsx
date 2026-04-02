import { RouterProvider } from "react-router-dom";
import { router } from "./routes.jsx";
import { Toaster } from "sonner";
import { AuthProvider } from "@/features/auth/context/AuthContext";

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
      <Toaster
        position="top-center"
        richColors
        duration={3000}
      />
    </AuthProvider>
  );
}
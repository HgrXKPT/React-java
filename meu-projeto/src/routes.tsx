import { createBrowserRouter } from "react-router-dom";
import { CreatePage } from "@/pages/Auth/Cadastro";
import { LoginPage } from "@/pages/Auth/Login";
import { DashboardPage } from "@/pages/Dashboard";
import { AppLayout } from "@/components/layout/AppLayout";
import { AuthLayout } from "@/components/layout/AuthLayout";

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [
      {
        path: "/login",
        element: <LoginPage />,
      },
      {
        path: "/criar",
        element: <CreatePage />,
      },
    ],
  },
  {
    element: <AppLayout />,
    children: [
      {
        index: true,
        element: <DashboardPage />,
      },
    ],
  },
]);

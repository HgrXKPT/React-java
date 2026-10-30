import { createBrowserRouter } from "react-router-dom";
import { CreatePage } from "../features/auth/pages/RegisterPage";
import { LoginPage } from "../features/auth/pages/LoginPage";
import { DashboardPage } from "../features/dashboard/pages/DashboardPage";
import { AppLayout } from "../components/layout/AppLayout";
import { AuthLayout } from "../features/auth/components/AuthLayout";

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

import { createBrowserRouter } from "react-router-dom";
import { CreatePage } from "../features/auth/pages/RegisterPage";
import { LoginPage } from "../features/auth/pages/LoginPage";

export const router = createBrowserRouter([
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/criar",

    element: <CreatePage />,
  },
]);

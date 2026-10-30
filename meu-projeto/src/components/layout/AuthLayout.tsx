import { Outlet } from "react-router-dom";

export function AuthLayout() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center gap-4 bg-gray-300">
      <Outlet />
    </main>
  );
}

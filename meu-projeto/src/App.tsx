import { RouterProvider } from "react-router-dom";
import "./App.css";
import { router } from "./routes/routes";
import { AuthProvider } from "./features/auth/context/AuthProvider";

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}

export default App;

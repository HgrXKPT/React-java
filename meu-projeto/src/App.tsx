import { RouterProvider } from "react-router-dom";
import "./App.css";
import { router } from "./routes/routes";

function App() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center gap-4 bg-gray-300">
      <RouterProvider router={router} />
    </main>
  );
}

export default App;

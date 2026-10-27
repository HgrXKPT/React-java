import "./App.css";
import { CreatePage } from "./features/auth/pages/RegisterPage";

function App() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center gap-4 bg-gray-300">
      <h1>Sistema</h1>

      <CreatePage />
    </main>
  );
}

export default App;

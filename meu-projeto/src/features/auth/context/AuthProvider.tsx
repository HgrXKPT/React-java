import { useState, type ReactNode } from "react";
import { AuthContext } from "./AuthContext";
import type { LogarUsuarioResponse } from "../types";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<LogarUsuarioResponse | null>(null);

  function entrar(novoUsuario: LogarUsuarioResponse) {
    setUsuario(novoUsuario);
  }

  function sair() {
    setUsuario(null);
  }

  return (
    <AuthContext value={{ usuario, entrar, sair }}>{children}</AuthContext>
  );
}

import { useState, type ReactNode } from "react";
import { AuthContext } from "./AuthContext";
import type { Usuario } from "@/models/usuario";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);

  function entrar(novoUsuario: Usuario) {
    setUsuario(novoUsuario);
  }

  function sair() {
    setUsuario(null);
  }

  return (
    <AuthContext value={{ usuario, entrar, sair }}>{children}</AuthContext>
  );
}

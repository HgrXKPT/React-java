import { useState, type ReactNode } from "react";
import { AuthContext } from "./AuthContext";
import type { Usuario } from "@/models/usuario";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(() => {
    const usuarioSalvo = localStorage.getItem("usuario");
    if (!usuarioSalvo) return null;
    return JSON.parse(usuarioSalvo);
  });

  function entrar(novoUsuario: Usuario) {
    setUsuario(novoUsuario);
    localStorage.setItem("usuario", JSON.stringify(novoUsuario));
  }

  function sair() {
    setUsuario(null);
    localStorage.removeItem("usuario");
  }

  return (
    <AuthContext value={{ usuario, entrar, sair }}>{children}</AuthContext>
  );
}

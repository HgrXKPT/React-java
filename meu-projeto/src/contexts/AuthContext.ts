import { createContext } from "react";
import type { Usuario } from "@/models/usuario";

export type AuthContextValue = {
  usuario: Usuario | null;
  entrar: (usuario: Usuario) => void;
  sair: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

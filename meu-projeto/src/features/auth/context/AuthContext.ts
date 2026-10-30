import { createContext } from "react";
import type { LogarUsuarioResponse } from "../types";

export type AuthContextValue = {
  usuario: LogarUsuarioResponse | null;
  entrar: (usuario: LogarUsuarioResponse) => void;
  sair: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

import type {
  LogarUsuarioRequest,
  Usuario,
  UsuarioOutputDTO,
} from "@/models/usuario";
import { toUsuario } from "./usuario.service";

class AuthService {
  baseUrl = "http://localhost:8080/Login";

  async logar(dados: LogarUsuarioRequest): Promise<Usuario> {
    const response = await fetch(`${this.baseUrl}/`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dados),
    });
    if (!response.ok) {
      throw new Error(`Erro na API: ${response.status}`);
    }

    const dto: UsuarioOutputDTO = await response.json();
    return toUsuario(dto);
  }
}

export const authService = new AuthService();

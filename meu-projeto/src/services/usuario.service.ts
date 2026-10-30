import { env } from "@/config/env";
import type {
  CriarUsuarioRequest,
  Usuario,
  UsuarioOutputDTO,
} from "@/models/usuario";

// traduz o que a API manda para o que o front usa
export function toUsuario(dto: UsuarioOutputDTO): Usuario {
  return {
    id: dto.id,
    email: dto.email,
    login: dto.login,
  };
}

class UsuarioService {
  baseUrl = `${env.apiBaseUrl}/Usuario`;

  async criar(dados: CriarUsuarioRequest): Promise<Usuario> {
    const response = await fetch(`${this.baseUrl}/Criar`, {
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

export const usuarioService = new UsuarioService();

import type { CriarUsuarioResponse, CriarUsuarioRequest } from "../types/index";

export class UsuarioApi {
  static baseUrl = "http://localhost:8080/Usuario";

  static async criar(
    dados: CriarUsuarioRequest,
  ): Promise<CriarUsuarioResponse> {
    const response = await fetch(`${this.baseUrl}/Criar`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dados),
    });
    if (!response.ok) {
      throw new Error(`Erro na API: ${response.status}`);
    }

    return await response.json();
  }
}

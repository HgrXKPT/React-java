import type { LogarUsuarioRequest, LogarUsuarioResponse } from "../types";

export class authApi {
  static baseUrl = "http://localhost:8080/Login";

  static async logar(
    dados: LogarUsuarioRequest,
  ): Promise<LogarUsuarioResponse> {
    const response = await fetch(`${this.baseUrl}/`, {
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

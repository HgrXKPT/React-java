import { useForm } from "react-hook-form";
import type { LogarUsuarioRequest } from "@/models/usuario";
import { authService } from "@/services/auth.service";
import { useAuth } from "@/hooks/use-auth";
import { useNavigate } from "react-router-dom";

export function useLoginPage() {
  const { entrar } = useAuth();
  const navigate = useNavigate();

  const { register, handleSubmit, setError } = useForm<LogarUsuarioRequest>();

  async function enviar(dados: LogarUsuarioRequest) {
    try {
      const result = await authService.logar(dados);
      entrar(result);
      navigate("/");
    } catch (error: unknown) {
      setError("root", {
        message: error instanceof Error ? error.message : "Erro inesperado.",
      });
    }
  }

  return { register, onSubmit: handleSubmit(enviar) };
}

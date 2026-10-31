import { usuarioService } from "@/services/usuario.service";
import { useForm } from "react-hook-form";
import type { CriarUsuarioRequest } from "@/models/usuario";
import { useNavigate } from "react-router-dom";

export function useCadastroPage() {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<CriarUsuarioRequest>();

  async function enviar(dados: CriarUsuarioRequest) {
    try {
      await usuarioService.criar(dados);
      navigate("/login");
    } catch (error: unknown) {
      setError("root", {
        message: error instanceof Error ? error.message : "Erro inesperado.",
      });
    }
  }

  return { register, errors, onSubmit: handleSubmit(enviar) };
}

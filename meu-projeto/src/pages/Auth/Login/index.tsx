import { useForm } from "react-hook-form";
import type { LogarUsuarioRequest } from "@/models/usuario";
import { authService } from "@/services/auth.service";
import { useAuth } from "@/hooks/use-auth";
import { useNavigate } from "react-router-dom";

export function LoginPage() {
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

  return (
    <div className="w-full max-w-sm p-6 bg-white rounded-lg shadow-md">
      <form
        onSubmit={handleSubmit(enviar)}
        noValidate
        action=""
        className="flex flex-col gap-4"
      >
        <div className="flex flex-col gap-1">
          <label htmlFor="email" className="text-sm font-medium text-gray-700">
            E-mail:
          </label>
          <input
            type="email"
            id="email"
            {...register("email", {
              required: "Informe o e-mail",
              pattern: {
                value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                message: "Formato de e-mail inválido",
              },
              minLength: { value: 3, message: "Minimo de 3 caracteres" },
              maxLength: {
                value: 255,
                message: "o e-mail deve ter no máximo 255 caracteres",
              },
            })}
            className="border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label htmlFor="senha" className="text-sm font-medium text-gray-700">
            Senha:
          </label>
          <input
            type="password"
            id="senha"
            {...register("senha", {
              required: "Campo de senha é obrigatório",
              minLength: { value: 3, message: "Minimo de 3 caracteres" },
              maxLength: {
                value: 64,
                message: "Número máximo de caracteres é 64",
              },
            })}
            className="border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="flex flex-col items-center gap-1">
          <button
            type="submit"
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded transition-colors cursor-pointer"
          >
            Entrar
          </button>
        </div>
      </form>
    </div>
  );
}

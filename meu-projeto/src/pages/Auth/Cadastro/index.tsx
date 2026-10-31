import { useCadastroPage } from "./hooks/use-cadastro-page";

export function CreatePage() {
  const { register, errors, onSubmit } = useCadastroPage();

  return (
    <div className="w-full max-w-sm p-6 bg-white rounded-lg shadow-md">
      <form
        onSubmit={onSubmit}
        noValidate
        className="flex flex-col gap-4"
      >
        <div className="flex flex-col gap-1">
          <label htmlFor="login" className="text-sm font-medium text-gray-700">
            Login:
          </label>
          <input
            type="text"
            id="login"
            {...register("login", {
              required: "Informe o Login",
              minLength: { value: 2, message: "Mínimo de 2 caractres" },
              validate: (v) => v.trim() !== "" || "Não pode ser só espaços",
            })}
            className="border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

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

        <div>
          {Object.values(errors)[0] && (
            <span role="alert">{Object.values(errors)[0].message}</span>
          )}{" "}
        </div>

        <div className="flex flex-col items-center gap-1">
          <button
            type="submit"
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded transition-colors cursor-pointer"
          >
            Enviar
          </button>
        </div>
      </form>
    </div>
  );
}

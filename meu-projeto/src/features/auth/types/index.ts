export type CriarUsuarioRequest = {
  email: string;
  login: string;
  senha: string;
};

export type CriarUsuarioResponse = {
  id: string;
  email: string;
  login: string;
};

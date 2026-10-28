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

export type LogarUsuarioRequest = {
  email: string;
  senha: string;
};

export type LogarUsuarioResponse = {
  id: string;
  email: string;
  senha: string;
};

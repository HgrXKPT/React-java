export type Usuario = {
  id: string;
  email: string;
  login: string;
};

export type UsuarioOutputDTO = {
  id: string;
  email: string;
  login: string;
};

export type CriarUsuarioRequest = {
  email: string;
  login: string;
  senha: string;
};

export type LogarUsuarioRequest = {
  email: string;
  senha: string;
};

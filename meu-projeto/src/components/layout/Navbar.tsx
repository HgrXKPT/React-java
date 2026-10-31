type NavbarProps = {
  titulo: string;
  login: string;
  email: string;
};

export function Navbar({ titulo, login, email }: NavbarProps) {
  const iniciais = login.slice(0, 2).toUpperCase();

  return (
    <header>
      <span>{titulo}</span>

      <div>
        <div>{iniciais}</div>

        <div>
          <span>{login}</span>
          <span>{email}</span>
        </div>
      </div>
    </header>
  );
}

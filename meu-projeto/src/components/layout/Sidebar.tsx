import { NavLink } from "react-router-dom";

export function Sidebar() {
  return (
    <aside>
      <div>
        <span>Logo</span>
        <span>Titulo</span>
      </div>

      <nav>
        <ul>
          <li>
            <NavLink to="/">1</NavLink>
          </li>
          <li>
            <NavLink to="/Importacoes">2</NavLink>
          </li>
          <li>
            <NavLink to="/Configuracoes">3</NavLink>
          </li>
        </ul>
      </nav>
    </aside>
  );
}

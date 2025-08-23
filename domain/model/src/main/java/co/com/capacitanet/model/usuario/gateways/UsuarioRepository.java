package co.com.capacitanet.model.usuario.gateways;

import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;

public interface UsuarioRepository {

    String registrarUsuario(Usuario usuario);

    String actualizarUsuario(ChangePassword usuario);

    String perfilUsuario(String userId);

    String login(Usuario usuario);

    String eliminarUsuario(Usuario usuario);

    String suscribirCurso(String userId, String idCurso);
}

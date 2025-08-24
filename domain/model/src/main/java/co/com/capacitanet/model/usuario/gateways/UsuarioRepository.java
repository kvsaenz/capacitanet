package co.com.capacitanet.model.usuario.gateways;

import co.com.capacitanet.model.curso.VerModulo;
import co.com.capacitanet.model.response.ResponseApp;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;

public interface UsuarioRepository {

    ResponseApp registrarUsuario(Usuario usuario);

    ResponseApp actualizarUsuario(ChangePassword usuario);

    ResponseApp perfilUsuario(String userId);

    ResponseApp login(Usuario usuario);

    ResponseApp eliminarUsuario(Usuario usuario);

    ResponseApp suscribirCurso(String userId, String idCurso);

    ResponseApp verModulo(String userId, VerModulo verModulo);
}

package co.com.capacitanet.usecase.registrarusuario;

import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public String registrarUsuario(Usuario usuario) {
        return usuarioRepository.registrarUsuario(usuario);
    }

    public String login(Usuario usuario) {
        return usuarioRepository.login(usuario);
    }
}

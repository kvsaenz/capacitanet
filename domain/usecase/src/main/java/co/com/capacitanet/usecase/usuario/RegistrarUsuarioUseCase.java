package co.com.capacitanet.usecase.usuario;

import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public String registrarUsuario(Usuario usuario) {
        return usuarioRepository.registrarUsuario(usuario);
    }

    public String actualizarUsuario(ChangePassword usuario) {
        Usuario usuario1 = Usuario.builder()
                .username(usuario.getUsername())
                .passwordHash(usuario.getPasswordHash())
                .build();
        String token = usuarioRepository.login(usuario1);
        if (token.split("\\.").length == 3) {
            return usuarioRepository.actualizarUsuario(usuario);
        } else {
            return "Cambios no autorizados";
        }
    }

    public String login(Usuario usuario) {
        return usuarioRepository.login(usuario);
    }
}

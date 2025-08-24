package co.com.capacitanet.usecase.usuario;

import co.com.capacitanet.model.curso.VerModulo;
import co.com.capacitanet.model.response.ResponseApp;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    private static final List<String> ALLOWED_DOMAINS = List.of("@bancodebogota.com.co");



    public ResponseApp registrarUsuario(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            return ResponseApp.builder().status(400)
                    .message("El username es obligatorio").build();
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return ResponseApp.builder().status(400)
                    .message("El nombre es obligatorio").build();
        }
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            return ResponseApp.builder().status(400)
                    .message("El apellido es obligatorio").build();
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            return ResponseApp.builder().status(400)
                    .message("La contraseña es obligatoria").build();
        }
        if (ALLOWED_DOMAINS.stream().noneMatch(usuario.getUsername()::endsWith)) {
            return ResponseApp.builder().status(400)
                    .message("El correo ingresado no pertenece al dominio corporativo.").build();
        }


        return usuarioRepository.registrarUsuario(usuario);
    }

    public ResponseApp actualizarUsuario(ChangePassword usuario) {
        Usuario usuario1 = Usuario.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .build();
        ResponseApp token = usuarioRepository.login(usuario1);
        if (token.getMessage().split("\\.").length == 3) {
            return usuarioRepository.actualizarUsuario(usuario);
        } else {
            return ResponseApp.builder().status(401).message("Cambios no autorizados").build();
        }
    }

    public ResponseApp eliminarUsuario(Usuario usuario) {
        ResponseApp token = usuarioRepository.login(usuario);
        if (token.getMessage().split("\\.").length == 3) {
            return usuarioRepository.eliminarUsuario(usuario);
        } else {
            return ResponseApp.builder().status(401).message("Cambios no autorizados").build();
        }
    }

    public ResponseApp perfilUsuario(String userId) {
        return usuarioRepository.perfilUsuario(userId);
    }

    public ResponseApp login(Usuario usuario) {
        return usuarioRepository.login(usuario);
    }

    public ResponseApp suscribirCurso(String userId, String idCurso) {
        return usuarioRepository.suscribirCurso(userId, idCurso);
    }

    public ResponseApp verModulo(String userId, VerModulo verModulo) {
        return usuarioRepository.verModulo(userId, verModulo);
    }
}

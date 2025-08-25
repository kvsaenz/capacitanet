package co.com.capacitanet.usecase.usuario;

import co.com.capacitanet.model.curso.VerModulo;
import co.com.capacitanet.model.response.ResponseApp;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.model.usuario.gateways.UsuarioRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Clase que contiene la lógica de negocio relacionada con los usuarios.
 * Proporciona métodos para registrar, actualizar, eliminar usuarios, entre otras operaciones.
 */
@RequiredArgsConstructor
public class UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    private static final List<String> ALLOWED_DOMAINS = List.of("@bancodebogota.com.co");


    /**
     * Registra un nuevo usuario después de validar los datos ingresados.
     *
     * @param usuario Objeto Usuario con la información del usuario a registrar.
     * @return Respuesta indicando el resultado de la operación.
     */
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

    /**
     * Actualiza la información de un usuario existente.
     *
     * @param usuario Objeto ChangePassword con la información del usuario y la nueva contraseña.
     * @return Respuesta indicando el resultado de la operación.
     */
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

    /**
     * Elimina (desactiva) un usuario existente.
     *
     * @param usuario Objeto Usuario con la información del usuario a eliminar.
     * @return Respuesta indicando el resultado de la operación.
     */
    public ResponseApp eliminarUsuario(Usuario usuario) {
        ResponseApp token = usuarioRepository.login(usuario);
        if (token.getMessage().split("\\.").length == 3) {
            return usuarioRepository.eliminarUsuario(usuario);
        } else {
            return ResponseApp.builder().status(401).message("Cambios no autorizados").build();
        }
    }

    /**
     * Obtiene el perfil de un usuario por su ID.
     *
     * @param userId ID del usuario a buscar.
     * @return Respuesta con la información del perfil del usuario.
     */
    public ResponseApp perfilUsuario(String userId) {
        return usuarioRepository.perfilUsuario(userId);
    }

    /**
     * Realiza el proceso de inicio de sesión para un usuario.
     *
     * @param usuario Objeto Usuario con las credenciales de inicio de sesión.
     * @return Respuesta indicando el resultado del inicio de sesión.
     */
    public ResponseApp login(Usuario usuario) {
        return usuarioRepository.login(usuario);
    }

    /**
     * Suscribe a un usuario a un curso específico.
     *
     * @param userId  ID del usuario a suscribir.
     * @param idCurso ID del curso al que se desea suscribir.
     * @return Respuesta indicando el resultado de la operación.
     */
    public ResponseApp suscribirCurso(String userId, String idCurso) {
        return usuarioRepository.suscribirCurso(userId, idCurso);
    }

    /**
     * Marca un módulo como visualizado para un usuario.
     *
     * @param userId    ID del usuario que visualizó el módulo.
     * @param verModulo Objeto VerModulo con la información del módulo visualizado.
     * @return Respuesta indicando el resultado de la operación.
     */
    public ResponseApp verModulo(String userId, VerModulo verModulo) {
        return usuarioRepository.verModulo(userId, verModulo);
    }
}

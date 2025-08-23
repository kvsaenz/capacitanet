package co.com.capacitanet.api;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.usecase.curso.CursoUseCase;
import co.com.capacitanet.usecase.usuario.UsuarioUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Controlador REST para manejar las operaciones relacionadas con usuarios y cursos.
 */
@RestController
@RequestMapping(value = "/capacitanet", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiRest {

    private static final Logger logger = LogManager.getLogger(ApiRest.class);
    private static final String USER_ID = "userId";

    private final UsuarioUseCase usuarioUseCase;
    private final CursoUseCase cursoUseCase;

    /**
     * Verifica el estado de salud del servicio.
     *
     * @return Un mensaje indicando que el servicio está activo.
     */
    @GetMapping(path = "/health")
    public String commandName() {
        return "hola";
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param usuario Objeto que contiene la información del usuario a registrar.
     * @return Mensaje de confirmación del registro.
     */
    @PostMapping(path = "/registrar-usuario")
    public String registrarUsuario(@RequestBody Usuario usuario) {
        logger.info("Iniciando registro de usuario: {}", usuario.getUsername());
        usuario.setActive(Boolean.TRUE);
        return usuarioUseCase.registrarUsuario(usuario);
    }

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param usuario Objeto que contiene la información del usuario y la nueva contraseña.
     * @return Mensaje de confirmación de la actualización.
     */
    @PostMapping(path = "/actualizar-password")
    public String actualizarUsuario(@RequestBody ChangePassword usuario) {
        logger.info("Iniciando actualizacion de password para el usuario: {}", usuario.getUsername());
        return usuarioUseCase.actualizarUsuario(usuario);
    }

    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Información del perfil del usuario.
     */
    @GetMapping(path = "/perfil-usuario")
    public String perfilUsuario(HttpServletRequest request) {
        String userId = (String) request.getAttribute(USER_ID);
        logger.info("Iniciando obtencion de perfil para el usuario: {}", userId);
        return usuarioUseCase.perfilUsuario(userId);
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param usuario Objeto que contiene la información del usuario a eliminar.
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Mensaje de confirmación o error si no está autorizado.
     */
    @PostMapping(path = "/borrar-usuario")
    public String borrarUsuario(@RequestBody Usuario usuario,
                                HttpServletRequest request) {
        logger.info("Iniciando borrado de usuario: {}", usuario.getUsername());
        String userId = (String) request.getAttribute(USER_ID);
        if (userId.equalsIgnoreCase(usuario.getUsername())) {
            return usuarioUseCase.eliminarUsuario(usuario);
        } else {
            return "Cambios no autorizados";
        }
    }

    /**
     * Inicia sesión para un usuario.
     *
     * @param usuario Objeto que contiene las credenciales del usuario.
     * @return Mensaje de confirmación del inicio de sesión.
     */
    @PostMapping(path = "/login")
    public String login(@RequestBody Usuario usuario) {
        logger.info("Iniciando login para el usuario: {}", usuario.getUsername());
        return usuarioUseCase.login(usuario);
    }

    /**
     * Crea un nuevo curso.
     *
     * @param curso   Objeto que contiene la información del curso a crear.
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Mensaje de confirmación de la creación del curso.
     */
    @PostMapping(path = "/crear-curso")
    public String crearCurso(@RequestBody Curso curso,
                             HttpServletRequest request) {
        logger.info("Iniciando creacion de curso: {}", curso.getTitulo());
        String userId = (String) request.getAttribute(USER_ID);
        curso.setCreadorUsername(userId);
        return cursoUseCase.crearCurso(curso);
    }

    /**
     * Suscribe al usuario autenticado a un curso.
     *
     * @param curso   Objeto que contiene la información del curso al que se desea suscribir.
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Mensaje de confirmación de la suscripción.
     */
    @PostMapping(path = "/suscribirme-curso")
    public String suscribirmeCurso(@RequestBody Curso curso,
                                   HttpServletRequest request) {
        logger.info("Iniciando suscripcion a curso: {}", curso.getCursoId());
        String userId = (String) request.getAttribute(USER_ID);

        return usuarioUseCase.suscribirCurso(userId, curso.getCursoId());
    }

    /**
     * Obtiene los cursos disponibles para el usuario autenticado.
     *
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Lista de cursos disponibles.
     */
    @GetMapping(path = "/obtener-cursos")
    public String obtenerCurso(HttpServletRequest request) {
        logger.info("Iniciando obtencion de cursos");
        String userId = (String) request.getAttribute(USER_ID);
        return cursoUseCase.obtenerCursos(userId);
    }

    /**
     * Sube un recurso a un curso específico.
     *
     * @param cursoId ID del curso al que se desea agregar el recurso.
     * @param file    Archivo que se desea subir.
     * @param order   Orden del recurso dentro del curso.
     * @param tipo    Tipo del recurso (por ejemplo, video, documento, etc.).
     * @return Mensaje de confirmación de la subida del recurso.
     * @throws IOException Si ocurre un error al manejar el archivo.
     */
    @PostMapping("/cursos/{cursoId}/recursos")
    public String subirRecurso(
            @PathVariable("cursoId") String cursoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("order") String order,
            @RequestParam("tipo") String tipo) throws IOException {

        // Crear un archivo temporal para almacenar el contenido del MultipartFile
        File temp = File.createTempFile("temp", file.getOriginalFilename());
        file.transferTo(temp);

        // Construir el objeto Recurso y agregarlo al curso
        cursoUseCase.agregarRecurso(cursoId, Recurso.builder()
                .id(UUID.randomUUID().toString())
                .order(Integer.parseInt(order))
                .visualizado(false)
                .tipo(tipo)
                .nombre(file.getOriginalFilename())
                .build(), temp);

        return "Recurso agregado al curso " + cursoId;
    }
}
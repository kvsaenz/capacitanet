package co.com.capacitanet.api;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.response.ResponseApp;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.usecase.curso.CursoUseCase;
import co.com.capacitanet.usecase.usuario.UsuarioUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @GetMapping(path = "/health", produces = "application/json")
    public ResponseEntity<ResponseApp> commandName() {
        return ResponseEntity.status(200).body(ResponseApp.builder().status(200).messaje("Healt Check CapacitaNet").build());
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param usuario Objeto que contiene la información del usuario a registrar.
     * @return Mensaje de confirmación del registro.
     */
    @PostMapping(path = "/registrar-usuario", produces = "application/json")
    public ResponseEntity<ResponseApp> registrarUsuario(@RequestBody Usuario usuario) {
        logger.info("Iniciando registro de usuario: {}", usuario.getUsername());
        usuario.setActive(Boolean.TRUE);
        ResponseApp response = usuarioUseCase.registrarUsuario(usuario);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param usuario Objeto que contiene la información del usuario y la nueva contraseña.
     * @return Mensaje de confirmación de la actualización.
     */
    @PostMapping(path = "/actualizar-password", produces = "application/json")
    public ResponseEntity<ResponseApp> actualizarUsuario(@RequestBody ChangePassword usuario) {
        logger.info("Iniciando actualizacion de password para el usuario: {}", usuario.getUsername());
        ResponseApp response = usuarioUseCase.actualizarUsuario(usuario);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Información del perfil del usuario.
     */
    @GetMapping(path = "/perfil-usuario", produces = "application/json")
    public ResponseEntity<Object> perfilUsuario(HttpServletRequest request) {
        String userId = (String) request.getAttribute(USER_ID);
        logger.info("Iniciando obtencion de perfil para el usuario: {}", userId);
        ResponseApp response = usuarioUseCase.perfilUsuario(userId);
        return ResponseEntity.status(response.getStatus()).body(response.getMessaje());
    }

    /**
     * Elimina un usuario del sistema.
     *
     * @param usuario Objeto que contiene la información del usuario a eliminar.
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Mensaje de confirmación o error si no está autorizado.
     */
    @PostMapping(path = "/borrar-usuario", produces = "application/json")
    public ResponseEntity<ResponseApp> borrarUsuario(@RequestBody Usuario usuario,
                                HttpServletRequest request) {
        logger.info("Iniciando borrado de usuario: {}", usuario.getUsername());
        String userId = (String) request.getAttribute(USER_ID);
        if (userId.equalsIgnoreCase(usuario.getUsername())) {
            ResponseApp response = usuarioUseCase.eliminarUsuario(usuario);
            return ResponseEntity.status(response.getStatus()).body(response);
        } else {
            return ResponseEntity.status(401).body(ResponseApp.builder()
                    .status(401).messaje("Cambios no autorizados").build());
        }
    }

    /**
     * Inicia sesión para un usuario.
     *
     * @param usuario Objeto que contiene las credenciales del usuario.
     * @return Mensaje de confirmación del inicio de sesión.
     */
    @PostMapping(path = "/login", produces = "application/json")
    public ResponseEntity<ResponseApp> login(@RequestBody Usuario usuario) {
        logger.info("Iniciando login para el usuario: {}", usuario.getUsername());
        ResponseApp response = usuarioUseCase.login(usuario);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Crea un nuevo curso.
     *
     * @param curso   Objeto que contiene la información del curso a crear.
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Mensaje de confirmación de la creación del curso.
     */
    @PostMapping(path = "/crear-curso", produces = "application/json")
    public ResponseEntity<ResponseApp> crearCurso(@RequestBody Curso curso,
                             HttpServletRequest request) {
        logger.info("Iniciando creacion de curso: {}", curso.getTitulo());
        String userId = (String) request.getAttribute(USER_ID);
        curso.setCreadorUsername(userId);
        ResponseApp response = cursoUseCase.crearCurso(curso);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Suscribe al usuario autenticado a un curso.
     *
     * @param curso   Objeto que contiene la información del curso al que se desea suscribir.
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Mensaje de confirmación de la suscripción.
     */
    @PostMapping(path = "/suscribirme-curso", produces = "application/json")
    public ResponseEntity<ResponseApp> suscribirmeCurso(@RequestBody Curso curso,
                                   HttpServletRequest request) {
        logger.info("Iniciando suscripcion a curso: {}", curso.getCursoId());
        String userId = (String) request.getAttribute(USER_ID);

        ResponseApp response = usuarioUseCase.suscribirCurso(userId, curso.getCursoId());
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Obtiene los cursos disponibles para el usuario autenticado.
     *
     * @param request Objeto HttpServletRequest que contiene información de la solicitud.
     * @return Lista de cursos disponibles.
     */
    @GetMapping(path = "/obtener-cursos", produces = "application/json")
    public ResponseEntity<Object> obtenerCurso(HttpServletRequest request) {
        logger.info("Iniciando obtencion de cursos");
        String userId = (String) request.getAttribute(USER_ID);
        ResponseApp response = cursoUseCase.obtenerCursos(userId);
        return ResponseEntity.status(response.getStatus()).body(response.getMessaje());
    }

    /**
     * Permite activar un curso que estaba en proceso de creacion
     *
     * @param curso   ID del curso a activar
     * @param request Objeto HttpServletRequest que contiene información de la solicitud
     * @return Mensaje de confirmación de la activación del curso
     */
    @PostMapping(path = "/activar-curso", produces = "application/json")
    public ResponseEntity<ResponseApp> activarCurso(@RequestBody Curso curso,
                                                    HttpServletRequest request) {
        logger.info("Iniciando activacion de curso: {}", curso.getCursoId());
        String userId = (String) request.getAttribute(USER_ID);
        ResponseApp response = cursoUseCase.activarCurso(curso.getCursoId(), userId);
        return ResponseEntity.status(response.getStatus()).body(response);
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
    public ResponseEntity<ResponseApp> subirRecurso(
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

        return ResponseEntity.status(200).body(ResponseApp
                .builder().status(200).messaje("Recurso agregado al curso " + cursoId).build());
    }
}
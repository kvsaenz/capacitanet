package co.com.capacitanet.api;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.usecase.curso.CursoUseCase;
import co.com.capacitanet.usecase.usuario.RegistrarUsuarioUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/capacitanet", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiRest {

    private static final Logger logger = LogManager.getLogger(ApiRest.class);


    RegistrarUsuarioUseCase registrarUsuarioUseCase;
    CursoUseCase cursoUseCase;

    @GetMapping(path = "/health")
    public String commandName() {
        return "hola";
    }

    @PostMapping(path = "/registrar-usuario")
    public String registrarUsuario(@RequestBody Usuario usuario) {
        logger.info("Iniciando registro de usuario: {}", usuario.getUsername());
        return registrarUsuarioUseCase.registrarUsuario(usuario);
    }

    @PostMapping(path = "/actualizar-password")
    public String actualizarUsuario(@RequestBody ChangePassword usuario) {
        logger.info("Iniciando actualizacion de password para el usuario: {}", usuario.getUsername());
        return registrarUsuarioUseCase.actualizarUsuario(usuario);
    }


    @PostMapping(path = "/login")
    public String login(@RequestBody Usuario usuario) {
        logger.info("Iniciando login para el usuario: {}", usuario.getUsername());
        return registrarUsuarioUseCase.login(usuario);
    }


    @PostMapping(path = "/crear-curso")
    public String crearCurso(@RequestBody Curso curso,
                             HttpServletRequest request) {
        logger.info("Iniciando creacion de curso: {}", curso.getTitulo());
        String userId = (String) request.getAttribute("userId");
        curso.setCreadorUsername(userId);
        return cursoUseCase.crearCurso(curso);
    }

}

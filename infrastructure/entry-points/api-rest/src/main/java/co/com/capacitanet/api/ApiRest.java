package co.com.capacitanet.api;

import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.usecase.usuario.RegistrarUsuarioUseCase;
import lombok.AllArgsConstructor;
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

    RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @GetMapping(path = "/health")
    public String commandName() {
        return "hola";
    }

    @PostMapping(path = "/registrar-usuario")
    public String registrarUsuario(@RequestBody Usuario usuario) {
        return registrarUsuarioUseCase.registrarUsuario(usuario);
    }

    @PostMapping(path = "/actualizar-usuario")
    public String actualizarUsuario(@RequestBody ChangePassword usuario) {
        return registrarUsuarioUseCase.actualizarUsuario(usuario);
    }


    @PostMapping(path = "/login")
    public String login(@RequestBody Usuario usuario) {
        return registrarUsuarioUseCase.login(usuario);
    }
}

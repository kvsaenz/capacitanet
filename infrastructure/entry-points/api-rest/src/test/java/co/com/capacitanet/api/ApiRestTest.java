package co.com.capacitanet.api;

import co.com.capacitanet.model.curso.VerModulo;
import co.com.capacitanet.model.response.ResponseApp;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.usecase.curso.CursoUseCase;
import co.com.capacitanet.usecase.usuario.UsuarioUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiRestTest {

    @Mock
    private UsuarioUseCase usuarioUseCase;

    @Mock
    private CursoUseCase cursoUseCase;

    @InjectMocks
    private ApiRest apiRest;

    @Mock
    private HttpServletRequest request;


    @Test
    void registrarUsuarioReturnsSuccessWhenValidUserProvided() {
        Usuario usuario = new Usuario();
        usuario.setUsername("user@bancodebogota.com.co");
        usuario.setActive(true);

        ResponseApp mockResponse = ResponseApp.builder().status(200).message("Usuario registrado").build();
        when(usuarioUseCase.registrarUsuario(usuario)).thenReturn(mockResponse);

        ResponseEntity<ResponseApp> response = apiRest.registrarUsuario(usuario);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Usuario registrado", response.getBody().getMessage());
    }


    @Test
    void visualizarModCursoReturnsSuccessWhenValidUserIdAndModuleProvided() {
        VerModulo verModulo = new VerModulo();
        verModulo.setRecursoId("mod123");
        when(request.getAttribute("userId")).thenReturn("user123");

        ResponseApp mockResponse = ResponseApp.builder().status(200).message("Módulo visualizado").build();
        when(usuarioUseCase.verModulo("user123", verModulo)).thenReturn(mockResponse);

        ResponseEntity<ResponseApp> response = apiRest.visualizarModCurso(verModulo, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Módulo visualizado", response.getBody().getMessage());
    }
}
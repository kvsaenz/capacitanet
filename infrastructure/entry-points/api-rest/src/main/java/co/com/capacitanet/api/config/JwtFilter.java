package co.com.capacitanet.api.config;

import co.com.capacitanet.helpers.AuthService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que se ejecuta una vez por solicitud para validar tokens de autenticación.
 * Este filtro verifica si las solicitudes contienen un token válido en el encabezado
 * de autorización y lo valida antes de permitir el acceso a los recursos protegidos.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String PATH_APP = "/capacitanet";
    private static final String ENCODING = "UTF-8";

    /**
     * Metodo que realiza el filtrado de las solicitudes HTTP.
     * Verifica si la solicitud es para un endpoint público o si contiene un token JWT válido.
     *
     * @param request     La solicitud HTTP entrante.
     * @param response    La respuesta HTTP saliente.
     * @param filterChain La cadena de filtros para continuar con el procesamiento.
     * @throws ServletException Si ocurre un error en el procesamiento del filtro.
     * @throws IOException      Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // Ignorar endpoints públicos
        if (path.startsWith(PATH_APP.concat("/health")) || path.startsWith(PATH_APP.concat("/registrar-usuario"))
                || path.startsWith(PATH_APP.concat("/actualizar-password")) || path.startsWith(PATH_APP.concat("/login"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(ENCODING);
            response.getWriter().write("Autorización requerida");
            return;
        }

        String token = header.substring(7);
        try {
            DecodedJWT decodedJWT = new AuthService().validarToken(token);
            request.setAttribute("userId", decodedJWT.getSubject());
            filterChain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(ENCODING);
            response.getWriter().write("Token expirado");
        } catch (JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(ENCODING);
            response.getWriter().write("Token inválido");
        }
    }
}

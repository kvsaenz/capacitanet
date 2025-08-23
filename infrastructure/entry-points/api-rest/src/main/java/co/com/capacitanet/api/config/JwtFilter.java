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

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static String PATH_APP = "/capacitanet";

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
            response.getWriter().write("Autorización requerida");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        String token = header.substring(7);
        try {
            DecodedJWT decodedJWT = new AuthService().validarToken(token);
            request.setAttribute("userId", decodedJWT.getSubject());
            filterChain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expirado");
            response.setCharacterEncoding("UTF-8");
        } catch (JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("Token inválido");
        }
    }
}

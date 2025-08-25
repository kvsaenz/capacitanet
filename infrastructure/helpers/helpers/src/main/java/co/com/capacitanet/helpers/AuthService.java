package co.com.capacitanet.helpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * Clase que proporciona servicios de autenticación utilizando JWT (JSON Web Tokens).
 * Permite generar, validar y extraer información de tokens JWT.
 */
public class AuthService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;


    /**
     * Constructor de la clase AuthService.
     * Inicializa el algoritmo de firma HMAC256 utilizando una clave secreta obtenida de las variables de entorno
     * y configura el verificador de tokens JWT.
     */
    public AuthService() {
        this.algorithm = Algorithm.HMAC256(System.getenv("SECRET_KEY"));
        this.verifier = JWT.require(algorithm).build();
    }

    /**
     * Genera un token JWT para un usuario específico.
     *
     * @param username El nombre de usuario para el cual se generará el token.
     * @return Un token JWT firmado que incluye el nombre de usuario, la fecha de emisión y la fecha de expiración.
     */
    public String generaJWT(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 600_000)) // 1h
                .sign(this.algorithm);
    }

    /**
     * Valida un token JWT y devuelve el token decodificado.
     *
     * @param token El token JWT a validar.
     * @return El token decodificado si es válido.
     * @throws com.auth0.jwt.exceptions.JWTVerificationException Si el token no es válido o ha expirado.
     */
    public DecodedJWT validarToken(String token) {
        return verifier.verify(token);
    }

    /**
     * Obtiene el nombre de usuario (subject) de un token JWT decodificado.
     *
     * @param jwt El token JWT decodificado.
     * @return El nombre de usuario contenido en el token.
     */
    public String getUsuario(DecodedJWT jwt) {
        return jwt.getSubject();
    }

}

package co.com.capacitanet.helpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class AuthService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;


    public AuthService() {
        this.algorithm = Algorithm.HMAC256(System.getenv("SECRET_KEY"));
        this.verifier = JWT.require(algorithm).build();
    }

    public String generaJWT(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600_000)) // 1h
                .sign(this.algorithm);
    }

    // Validar token y devolver el JWT decodificado
    public DecodedJWT validarToken(String token) {
        return verifier.verify(token);
    }

    public String getUsuario(DecodedJWT jwt) {
        return jwt.getSubject();
    }

}

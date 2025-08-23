package co.com.capacitanet.helpers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class Password {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public static boolean verificar(String plainPassword, String hash) {
        return encoder.matches(plainPassword, hash);
    }

}

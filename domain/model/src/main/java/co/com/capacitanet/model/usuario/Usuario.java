package co.com.capacitanet.model.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Usuario {

    private String username;
    private String nombre;
    private String apellido;
    private String password;
    private boolean active;
    private List<String> cursos;
    private List<String> insignias;

}

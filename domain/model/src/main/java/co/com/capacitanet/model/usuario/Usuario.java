package co.com.capacitanet.model.usuario;

import co.com.capacitanet.model.curso.Curso;
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
    private List<Curso> cursos;
    private List<String> insignias;

    public List<Curso> getCursos() {
        if (cursos == null) {
            return List.of();
        }
        return cursos;
    }

    public List<String> getInsignias() {
        if (insignias == null) {
            return List.of();
        }
        return insignias;
    }


}

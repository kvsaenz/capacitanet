package co.com.capacitanet.model.curso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Curso {

    private String cursoId;
    private String titulo;
    private String descripcion;
    private String creadorUsername;
    private boolean active;
    private List<String> tags;
    private List<Recurso> recursos;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags;
    }

    public List<Recurso> getRecursos() {
        if (recursos == null) {
            return new ArrayList<>();
        }
        return recursos;
    }
}

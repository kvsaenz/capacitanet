package co.com.capacitanet.model.curso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Recurso {
    private String id;
    private int order;
    private boolean visualizado;
    private String tipo;
    private String nombre;
    private String s3Key;
}

package co.com.capacitanet.model.curso.gateways;


import co.com.capacitanet.model.curso.Curso;

import java.util.List;

public interface CursoRepository {

    String crearCurso(Curso curso);

    List<String> obtenerCursos();

}

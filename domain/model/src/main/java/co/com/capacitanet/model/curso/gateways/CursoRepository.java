package co.com.capacitanet.model.curso.gateways;


import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;

import java.io.File;

public interface CursoRepository {

    String crearCurso(Curso curso);

    String obtenerCursos(String userId);

    String agregarRecurso(String cursoId, Recurso recurso, File file);
}

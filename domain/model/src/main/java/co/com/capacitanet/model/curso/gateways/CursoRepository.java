package co.com.capacitanet.model.curso.gateways;


import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.response.ResponseApp;

import java.io.File;

public interface CursoRepository {

    ResponseApp crearCurso(Curso curso);

    ResponseApp obtenerCursos(String userId, boolean estado);

    ResponseApp agregarRecurso(String cursoId, Recurso recurso, File file);

    ResponseApp activarCurso(String cursoId, String userId);

}

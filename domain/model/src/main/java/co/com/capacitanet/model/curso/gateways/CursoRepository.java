package co.com.capacitanet.model.curso.gateways;


import co.com.capacitanet.model.curso.Curso;

public interface CursoRepository {

    String crearCurso(Curso curso);

    String obtenerCursos(String userId);

}

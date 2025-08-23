package co.com.capacitanet.usecase.curso;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.curso.gateways.CursoRepository;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class CursoUseCase {

    private final CursoRepository cursoRepository;


    public String crearCurso(Curso curso) {
        return cursoRepository.crearCurso(curso);
    }

    public String obtenerCursos(String userId) {
        return cursoRepository.obtenerCursos(userId);
    }

    public String agregarRecurso(String cursoId, Recurso recurso, File file) {
        return cursoRepository.agregarRecurso(cursoId, recurso, file);
    }

}

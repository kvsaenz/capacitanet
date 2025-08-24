package co.com.capacitanet.usecase.curso;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.curso.gateways.CursoRepository;
import co.com.capacitanet.model.response.ResponseApp;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class CursoUseCase {

    private final CursoRepository cursoRepository;


    public ResponseApp crearCurso(Curso curso) {
        return cursoRepository.crearCurso(curso);
    }

    public ResponseApp obtenerCursos(String userId) {
        return cursoRepository.obtenerCursos(userId);
    }

    public ResponseApp agregarRecurso(String cursoId, Recurso recurso, File file) {
        return cursoRepository.agregarRecurso(cursoId, recurso, file);
    }

    public ResponseApp activarCurso(String cursoId, String userId) {
        return cursoRepository.activarCurso(cursoId, userId);
    }
}

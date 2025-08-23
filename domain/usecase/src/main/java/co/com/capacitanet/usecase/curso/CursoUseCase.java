package co.com.capacitanet.usecase.curso;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.gateways.CursoRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CursoUseCase {

    private final CursoRepository cursoRepository;


    public String crearCurso(Curso curso) {
        return cursoRepository.crearCurso(curso);
    }

    public String obtenerCursos(String userId) {
        return cursoRepository.obtenerCursos(userId);
    }

}

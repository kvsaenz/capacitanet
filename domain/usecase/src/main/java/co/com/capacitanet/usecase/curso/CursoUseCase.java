package co.com.capacitanet.usecase.curso;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.curso.gateways.CursoRepository;
import co.com.capacitanet.model.response.ResponseApp;
import lombok.RequiredArgsConstructor;

import java.io.File;

/**
 * Clase que contiene la lógica de negocio relacionada con los cursos.
 * Proporciona métodos para crear, obtener, agregar recursos y activar cursos.
 */
@RequiredArgsConstructor
public class CursoUseCase {

    private final CursoRepository cursoRepository;

    /**
     * Crea un nuevo curso.
     *
     * @param curso Objeto Curso con la información del curso a crear.
     * @return Respuesta indicando el resultado de la operación.
     */
    public ResponseApp crearCurso(Curso curso) {
        return cursoRepository.crearCurso(curso);
    }

    /**
     * Obtiene la lista de cursos asociados a un usuario.
     *
     * @param userId ID del usuario para el cual se obtendrán los cursos.
     * @param estado Estado de los cursos a filtrar (activos/inactivos).
     * @return Respuesta con la lista de cursos obtenidos.
     */
    public ResponseApp obtenerCursos(String userId, boolean estado) {
        return cursoRepository.obtenerCursos(userId, estado);
    }

    /**
     * Agrega un recurso a un curso existente.
     *
     * @param cursoId ID del curso al que se agregará el recurso.
     * @param recurso Objeto Recurso con la información del recurso a agregar.
     * @param file    Archivo asociado al recurso.
     * @return Respuesta indicando el resultado de la operación.
     */
    public ResponseApp agregarRecurso(String cursoId, Recurso recurso, File file) {
        return cursoRepository.agregarRecurso(cursoId, recurso, file);
    }

    /**
     * Activa un curso para un usuario específico.
     *
     * @param cursoId ID del curso a activar.
     * @param userId  ID del usuario para el cual se activará el curso.
     * @return Respuesta indicando el resultado de la operación.
     */
    public ResponseApp activarCurso(String cursoId, String userId) {
        return cursoRepository.activarCurso(cursoId, userId);
    }

}

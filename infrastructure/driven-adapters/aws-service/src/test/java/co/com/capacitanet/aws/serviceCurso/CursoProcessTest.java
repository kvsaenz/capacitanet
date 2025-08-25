package co.com.capacitanet.aws.serviceCurso;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.response.ResponseApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CursoProcessTest {

    private DynamoDbClient dynamoDbClient;
    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private CursoProcess cursoProcess;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);
        cursoProcess = new CursoProcess(dynamoDbClient, s3Client, s3Presigner);
    }

    @Nested
    @DisplayName("obtenerCursos")
    class ObtenerCursos {

        @Test
        @DisplayName("Should return active courses for a user")
        void shouldReturnActiveCoursesForUser() {
            ScanResponse scanResponse = ScanResponse.builder()
                    .items(List.of(
                            Map.of("datosCurso", AttributeValue.builder().s("{\"cursoId\":\"1\",\"active\":true,\"recursos\":[]}").build())
                    ))
                    .build();
            when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(scanResponse);

            ResponseApp response = cursoProcess.obtenerCursos("user1", true);

            assertEquals(200, response.getStatus());
            assertTrue(response.getMessage().contains("cursoId"));
        }

        @Test
        @DisplayName("Should return error when JSON conversion fails")
        void shouldReturnErrorWhenJsonConversionFails() {
            ScanResponse scanResponse = ScanResponse.builder()
                    .items(List.of(
                            Map.of("datosCurso", AttributeValue.builder().s("invalid-json").build())
                    ))
                    .build();
            when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(scanResponse);

            ResponseApp response = cursoProcess.obtenerCursos("user1", true);

            assertEquals(500, response.getStatus());
            assertEquals("Error al convertir el JSON a Curso", response.getMessage());
        }
    }

    @Nested
    @DisplayName("crearCurso")
    class CrearCurso {

        @Test
        @DisplayName("Should create a course successfully")
        void shouldCreateCourseSuccessfully() {
            Curso curso = new Curso();
            curso.setCursoId("1");

            when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());

            ResponseApp response = cursoProcess.crearCurso(curso);

            assertEquals(200, response.getStatus());
            assertEquals("Curso registrado satisfactoriamente", response.getMessage());
        }

        @Test
        @DisplayName("Should return error when course already exists")
        void shouldReturnErrorWhenCourseAlreadyExists() {
            Curso curso = new Curso();
            curso.setCursoId("1");

            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenThrow(ConditionalCheckFailedException.builder().build());

            ResponseApp response = cursoProcess.crearCurso(curso);

            assertEquals(200, response.getStatus());
            assertEquals("El curso ya está registrado", response.getMessage());
        }
    }

}
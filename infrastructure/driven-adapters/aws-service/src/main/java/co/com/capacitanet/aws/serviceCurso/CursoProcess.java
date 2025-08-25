package co.com.capacitanet.aws.serviceCurso;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.curso.gateways.CursoRepository;
import co.com.capacitanet.model.response.ResponseApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase que implementa la interfaz CursoRepository para manejar la lógica de negocio
 * relacionada con los cursos. Esta clase interactúa con DynamoDB y S3 para almacenar
 * y recuperar información de los cursos y sus recursos.
 */

@Component
public class CursoProcess implements CursoRepository {

    private static final Logger logger = LogManager.getLogger(CursoProcess.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CLAVE = "cursoId";
    private static final String DATOS = "datosCurso";
    private static final String TABLE_NAME = "capacitanet_cursos";
    private static final String BUCKET_NAME = "capacitanet-resource";

    private final DynamoDbClient client;
    private final S3Client s3Client;
    private final S3Presigner presigner;

    /**
     * Constructor de la clase CursoProcess.
     *
     * @param client    Cliente de DynamoDB para interactuar con la base de datos.
     * @param s3Client  Cliente de S3 para interactuar con el almacenamiento de objetos.
     * @param presigner Cliente para generar URLs prefirmadas de S3.
     */
    public CursoProcess(DynamoDbClient client, S3Client s3Client, S3Presigner presigner) {
        this.client = client;
        this.s3Client = s3Client;
        this.presigner = presigner;
    }


    /**
     * Obtiene los cursos disponibles o inactivos según el estado.
     *
     * @param userId ID del usuario que solicita los cursos.
     * @param estado Estado de los cursos a filtrar (activos/inactivos).
     * @return Respuesta con la lista de cursos o un mensaje de error.
     */
    @Override
    public ResponseApp obtenerCursos(String userId, boolean estado) {
        ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        ScanResponse response = client.scan(request);

        List<Curso> cursos = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            String json = item.get(DATOS).s();
            try {
                Curso curso = new ObjectMapper().readValue(json, Curso.class);
                if (curso.isActive() == estado) {
                    for (Recurso rec : curso.getRecursos()) {
                        String url = generarS3Url(rec.getS3Key());
                        rec.setS3Key(url);
                    }
                    if (estado) {
                        cursos.add(curso);
                    } else {
                        if (curso.getCreadorUsername().equalsIgnoreCase(userId)) {
                            cursos.add(curso);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error al convertir el JSON a Curso: {}", e.getMessage());
                return ResponseApp.builder().status(500).message("Error al convertir el JSON a Curso").build();
            }
        }
        try {
            return ResponseApp.builder().status(200).message(mapper.writeValueAsString(cursos)).build();
        } catch (JsonProcessingException e) {
            return ResponseApp.builder().status(500).message("Error al convertir los cursos a JSON").build();
        }
    }

    /**
     * Crea un nuevo curso en la base de datos.
     *
     * @param curso Objeto Curso con la información del curso a registrar.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp crearCurso(Curso curso) {
        try {
            String json = mapper.writeValueAsString(curso);
            Map<String, AttributeValue> item = new HashMap<>();
            item.put(CLAVE, AttributeValue.builder().s(curso.getCursoId()).build());
            item.put(DATOS, AttributeValue.builder().s(json).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .conditionExpression("attribute_not_exists(" + CLAVE + ")")
                    .build();

            client.putItem(request);
            logger.info("Curso registrado exitosamente: {}", curso.getCursoId());
            return ResponseApp.builder().status(200).message("Curso registrado satisfactoriamente").build();
        } catch (JsonProcessingException e) {
            logger.error("Error al convertir un curso a JSON: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error en el formato del curso").build();
        } catch (ConditionalCheckFailedException e) {
            logger.error("El curso ya existe: {}", e.getMessage());
            return ResponseApp.builder().status(200).message("El curso ya está registrado").build();
        } catch (Exception e) {
            logger.error("Error al registrar el curso: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al registrar el curso").build();
        }
    }

    /**
     * Agrega un recurso a un curso existente.
     *
     * @param cursoId ID del curso al que se desea agregar el recurso.
     * @param recurso Objeto Recurso con la información del recurso a agregar.
     * @param file    Archivo asociado al recurso.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp agregarRecurso(String cursoId, Recurso recurso, File file) {
        try {
            String keys3 = "cursos/" + cursoId + "/" + file.getName();

            s3Client.putObject(builder -> builder.bucket(BUCKET_NAME).key(keys3).build(),
                    file.toPath());


            recurso.setS3Key(keys3);

            Map<String, AttributeValue> key = new HashMap<>();
            key.put(CLAVE, AttributeValue.builder().s(cursoId).build());
            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(DATOS).s();
                Curso cursoDB = mapper.readValue(jsonData, Curso.class);
                cursoDB.getRecursos().add(recurso);

                String json = mapper.writeValueAsString(cursoDB);

                UpdateItemRequest request = UpdateItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .key(key)
                        .updateExpression("SET " + DATOS + " = :" + DATOS)
                        .expressionAttributeValues(Map.of(
                                ":" + DATOS, AttributeValue.builder().s(json).build()
                        ))
                        .build();

                client.updateItem(request);
                logger.info("Recurso agregado al curso: {}", cursoId);
                return ResponseApp.builder().status(200).message("Recurso agregado satisfactoriamente").build();
            } else {
                logger.error("Curso no encontrado: {}", cursoId);
                return ResponseApp.builder().status(404).message("Curso no encontrado").build();
            }
        } catch (JsonProcessingException e) {
            logger.error("Error al convertir el curso a JSON: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error en el formato del curso").build();
        } catch (Exception e) {
            logger.error("Error al agregar el recurso: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al agregar el recurso").build();
        }
    }


    /**
     * Activa o desactiva un curso según su estado actual.
     *
     * @param cursoId ID del curso a activar o desactivar.
     * @param userId  ID del usuario que realiza la operación.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp activarCurso(String cursoId, String userId) {
        try {

            Map<String, AttributeValue> key = new HashMap<>();
            key.put(CLAVE, AttributeValue.builder().s(cursoId).build());
            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(DATOS).s();
                Curso cursoDB = mapper.readValue(jsonData, Curso.class);
                if (!cursoDB.getCreadorUsername().equalsIgnoreCase(userId)) {
                    return ResponseApp.builder().status(401).message("Cambios no autorizados").build();
                }
                cursoDB.setActive(!cursoDB.isActive());
                String json = mapper.writeValueAsString(cursoDB);

                UpdateItemRequest request = UpdateItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .key(key)
                        .updateExpression("SET " + DATOS + " = :" + DATOS)
                        .expressionAttributeValues(Map.of(
                                ":" + DATOS, AttributeValue.builder().s(json).build()
                        ))
                        .build();

                client.updateItem(request);
                logger.info("Curso activado: {}", cursoId);
                return ResponseApp.builder().status(200).message("Curso activado satisfactoriamente").build();

            } else {
                logger.error("Curso no encontrado para activar: {}", cursoId);
                return ResponseApp.builder().status(404).message("Curso no encontrado").build();
            }
        } catch (Exception e) {
            logger.error("Error al activar el recurso: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al activar el curso").build();
        }
    }

    /**
     * Genera una URL prefirmada para acceder a un recurso en S3.
     *
     * @param key Clave del recurso en S3.
     * @return URL prefirmada o null en caso de error.
     */
    private String generarS3Url(String key) {
        try {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(presignRequest);
            String url = presignedGetObjectRequest.url().toString();

            logger.info("URL de S3 generada: {}", url);
            return url;
        } catch (Exception e) {
            logger.error("Error al generar la URL de S3: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene un curso por su ID.
     *
     * @param cursoId ID del curso a buscar.
     * @return Objeto Curso o null si no se encuentra.
     */
    public Curso obtenerCursoPorId(String cursoId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(CLAVE, AttributeValue.builder().s(cursoId).build());
        var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
        if (response.hasItem()) {
            String json = response.item().get(DATOS).s();
            try {
                return new ObjectMapper().readValue(json, Curso.class);
            } catch (Exception e) {
                logger.error("Error al convertir el JSON a Curso: {}", e.getMessage());
            }
        }
        return null;
    }
}

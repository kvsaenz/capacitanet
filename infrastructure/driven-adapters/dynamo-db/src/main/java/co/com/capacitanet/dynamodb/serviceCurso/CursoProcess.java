package co.com.capacitanet.dynamodb.serviceCurso;

import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.gateways.CursoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CursoProcess implements CursoRepository {

    private static final Logger logger = LogManager.getLogger(CursoProcess.class);


    private static final String CLAVE = "cursoId";
    private static final String DATOS = "datosCurso";
    private static final String TABLE_NAME = "capacitanet_cursos";

    private final DynamoDbClient client;

    public CursoProcess(DynamoDbClient client) {
        this.client = client;
    }


    @Override
    public List<String> obtenerCursos() {
        return List.of();
    }

    @Override
    public String crearCurso(Curso curso) {
        ObjectMapper mapper = new ObjectMapper();
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
            return "Curso registrado satisfactoriamente";
        } catch (JsonProcessingException e) {
            logger.error("Error al convertir el curso a JSON: {}", e.getMessage());
            return "Error en el formato del curso";
        } catch (ConditionalCheckFailedException e) {
            logger.error("El curso ya existe: {}", e.getMessage());
            return "El curso ya está registrado";
        } catch (Exception e) {
            logger.error("Error al registrar el curso: {}", e.getMessage());
            return "Error al registrar el curso";
        }
    }
}

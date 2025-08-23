package co.com.capacitanet.dynamodb.serviceUser;

import co.com.capacitanet.dynamodb.serviceCurso.CursoProcess;
import co.com.capacitanet.helpers.AuthService;
import co.com.capacitanet.helpers.Password;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.model.usuario.gateways.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class UsersProcess implements UsuarioRepository {

    private static final Logger logger = LogManager.getLogger(UsersProcess.class);

    private final CursoProcess cursoProcess;

    private static final String USERNAME = "username";
    private static final String PERFIL = "perfil";
    private static final String TABLE_NAME = "capacitanet_user";

    private final DynamoDbClient client;

    public UsersProcess(CursoProcess cursoProcess, DynamoDbClient client) {
        this.cursoProcess = cursoProcess;
        this.client = client;
    }


    @Override
    public String registrarUsuario(Usuario usuario) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            usuario.setPassword(Password.hash(usuario.getPassword()));
            String json = mapper.writeValueAsString(usuario);
            Map<String, AttributeValue> item = new HashMap<>();
            item.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());
            item.put(PERFIL, AttributeValue.builder().s(json).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .conditionExpression("attribute_not_exists(username)")
                    .build();

            client.putItem(request);
            logger.info("Usuario registrado: {}", usuario.getUsername());
            return "Usuario registrado exitosamente";
        } catch (JsonProcessingException e) {
            logger.error("Error al procesar los datos del usuario: {}", e.getMessage());
            return "Usuario no registrado";
        } catch (ConditionalCheckFailedException e) {
            logger.error("El usuario ya existe: {}", usuario.getUsername());
            return "Usuario ya existe";
        } catch (Exception e) {
            logger.error("Error al registrar el usuario: {}", e.getMessage());
            return "Usuario no registrado satisfactoriamente";
        }
    }

    @Override
    public String actualizarUsuario(ChangePassword usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                ObjectMapper mapper = new ObjectMapper();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);
                storedUser.setPassword(Password.hash(usuario.getPasswordNew()));

                String json = mapper.writeValueAsString(storedUser);

                UpdateItemRequest request = UpdateItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .key(key)
                        .updateExpression("SET perfil = :perfil")
                        .expressionAttributeValues(Map.of(
                                ":perfil", AttributeValue.builder().s(json).build()
                        ))
                        .build();

                client.updateItem(request);
                logger.info("Contraseña actualizada para el usuario: {}", usuario.getUsername());
                return "Contraseña actualizada exitosamente";
            } else {
                logger.info("Usuario no existe para actualizar: {}", usuario.getUsername());
                return "Usuario no exite";
            }
        } catch (Exception e) {
            logger.error("Error al actualizar la contraseña: {}", e.getMessage());
            return "Error al actualizar la contraseña";
        }
    }

    @Override
    public String perfilUsuario(String userId) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(userId).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                ObjectMapper mapper = new ObjectMapper();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);
                storedUser.setPassword("********");
                return mapper.writeValueAsString(storedUser);
            } else {
                logger.info("Usuario no existe para obtener perfil: {}", userId);
                return "Usuario no exite";
            }
        } catch (JsonProcessingException e) {
            logger.error("Error al procesar los datos en perfil del usuario: {}", e.getMessage());
            return "Error al procesar los datos del usuario";
        } catch (Exception e) {
            logger.error("Error al obtener el perfil del usuario: {}", e.getMessage());
            return "Error al obtener el perfil del usuario";
        }
    }

    @Override
    public String login(Usuario usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                ObjectMapper mapper = new ObjectMapper();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);

                if (storedUser.isActive()) {
                    if (Password.verificar(usuario.getPassword(), storedUser.getPassword())) {
                        logger.info("Login exitoso para el usuario: {}", usuario.getUsername());
                        return new AuthService().generaJWT(usuario.getUsername());
                    } else {
                        logger.info("Credenciales incorrectas para el usuario: {}", usuario.getUsername());
                        return "Credenciales incorrectas";
                    }
                } else {
                    logger.info("Usuario inactivo: {}", usuario.getUsername());
                    return "Usuario inactivo";
                }
            } else {
                logger.info("Usuario no existe: {}", usuario.getUsername());
                return "Usuario no exite";
            }
        } catch (JsonProcessingException e) {
            logger.error("Error al procesar los datos en login del usuario: {}", e.getMessage());
            return "Error al procesar los datos del usuario";
        } catch (Exception e) {
            logger.error("Error en el proceso de login: {}", e.getMessage());
            return "Error en el proceso de login";
        }
    }

    @Override
    public String eliminarUsuario(Usuario usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                ObjectMapper mapper = new ObjectMapper();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);
                storedUser.setActive(false);

                String json = mapper.writeValueAsString(storedUser);

                UpdateItemRequest request = UpdateItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .key(key)
                        .updateExpression("SET perfil = :perfil")
                        .expressionAttributeValues(Map.of(
                                ":perfil", AttributeValue.builder().s(json).build()
                        ))
                        .build();

                client.updateItem(request);
                logger.info("Usuario desactivado: {}", usuario.getUsername());
                return "Usuario desactivado exitosamente";
            } else {
                logger.info("Usuario no existe para eliminar: {}", usuario.getUsername());
                return "Usuario no exite";
            }
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario: {}", e.getMessage());
            return "Error al eliminar el usuario";
        }
    }

    @Override
    public String suscribirCurso(String userId, String idCurso) {
        try {
            // buscar el usuario en la base de datos
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(userId).build());
            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                ObjectMapper mapper = new ObjectMapper();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);

                // verificar si el curso ya está en la lista de cursos suscritos
                if (storedUser.getCursos() != null && storedUser.getCursos().contains(idCurso)) {
                    logger.info("El usuario ya está suscrito al curso: {}", idCurso);
                    return "Ya estás suscrito a este curso";
                } else {
                    // agregar el curso a la lista de cursos suscritos
                    storedUser.getCursos().add(cursoProcess.obtenerCursoPorId(idCurso));

                    String json = mapper.writeValueAsString(storedUser);

                    UpdateItemRequest request = UpdateItemRequest.builder()
                            .tableName(TABLE_NAME)
                            .key(key)
                            .updateExpression("SET perfil = :perfil")
                            .expressionAttributeValues(Map.of(
                                    ":perfil", AttributeValue.builder().s(json).build()
                            ))
                            .build();

                    client.updateItem(request);
                    logger.info("Usuario {} suscrito al curso: {}", userId, idCurso);
                    return "Suscripción al curso exitosa";
                }
            } else {
                logger.info("Usuario no existe para suscribir al curso: {}", userId);
                return "Usuario no exite";
            }
        } catch (Exception e) {
            logger.error("Error al suscribir el curso: {}", e.getMessage());
        }
        return "";
    }
}

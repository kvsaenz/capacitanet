package co.com.capacitanet.aws.serviceUser;

import co.com.capacitanet.aws.serviceCurso.CursoProcess;
import co.com.capacitanet.helpers.AuthService;
import co.com.capacitanet.helpers.Password;
import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.curso.Recurso;
import co.com.capacitanet.model.curso.VerModulo;
import co.com.capacitanet.model.response.ResponseApp;
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

/**
 * Clase que implementa la interfaz UsuarioRepository para manejar la lógica de negocio
 * relacionada con los usuarios. Esta clase interactúa con DynamoDB para almacenar y
 * recuperar información de los usuarios.
 */
@Component
public class UsersProcess implements UsuarioRepository {

    private static final Logger logger = LogManager.getLogger(UsersProcess.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private final CursoProcess cursoProcess;

    private static final String USERNAME = "username";
    private static final String PERFIL = "perfil";
    private static final String TABLE_NAME = "capacitanet_user";

    private final DynamoDbClient client;

    /**
     * Clase que implementa la interfaz UsuarioRepository para manejar la lógica de negocio
     * relacionada con los usuarios. Esta clase interactúa con DynamoDB para almacenar y
     * recuperar información de los usuarios.
     */
    public UsersProcess(CursoProcess cursoProcess, DynamoDbClient client) {
        this.cursoProcess = cursoProcess;
        this.client = client;
    }


    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param usuario Objeto Usuario con la información del usuario a registrar.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp registrarUsuario(Usuario usuario) {

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
            return ResponseApp.builder().status(200).message("Registro exitoso").build();
        } catch (JsonProcessingException e) {
            return ResponseApp.builder().status(500).message("Usuario no registrado").build();
        } catch (ConditionalCheckFailedException e) {
            logger.error("El usuario o correo ya está registrado en el sistema. {}", usuario.getUsername());
            return ResponseApp.builder().status(401).message("El usuario o correo ya está registrado en el sistema.").build();
        } catch (Exception e) {
            logger.error("Error al registrar el usuario: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Usuario no registrado satisfactoriamente").build();
        }
    }

    /**
     * Actualiza la contraseña de un usuario existente.
     *
     * @param usuario Objeto ChangePassword con la información del usuario y la nueva contraseña.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp actualizarUsuario(ChangePassword usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
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
                return ResponseApp.builder().status(200).message("Contraseña actualizada exitosamente").build();
            } else {
                logger.info("Usuario no existe para actualizar: {}", usuario.getUsername());
                return ResponseApp.builder().status(404).message("Usuario no exite").build();
            }
        } catch (Exception e) {
            logger.error("Error al actualizar la contraseña: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al actualizar la contraseña").build();
        }
    }

    /**
     * Obtiene el perfil de un usuario por su ID.
     *
     * @param userId ID del usuario a buscar.
     * @return Respuesta con la información del perfil del usuario o un mensaje de error.
     */
    @Override
    public ResponseApp perfilUsuario(String userId) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(userId).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);
                storedUser.setPassword("********");
                return ResponseApp.builder().status(200).message(mapper.writeValueAsString(storedUser)).build();
            } else {
                logger.info("Usuario no existe para obtener perfil: {}", userId);
                return ResponseApp.builder().status(404).message("Usuario no exite").build();
            }
        } catch (JsonProcessingException e) {
            logger.error("Error al procesar los datos en perfil del usuario: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al procesar los datos del usuario").build();

        } catch (Exception e) {
            logger.error("Error al obtener el perfil del usuario: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al obtener el perfil del usuario").build();

        }
    }

    /**
     * Realiza el proceso de inicio de sesión para un usuario.
     *
     * @param usuario Objeto Usuario con las credenciales de inicio de sesión.
     * @return Respuesta indicando el resultado del inicio de sesión.
     */
    @Override
    public ResponseApp login(Usuario usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);

                if (storedUser.isActive()) {
                    if (Password.verificar(usuario.getPassword(), storedUser.getPassword())) {
                        logger.info("Login exitoso para el usuario: {}", usuario.getUsername());
                        return ResponseApp.builder().status(200).message(new AuthService().generaJWT(usuario.getUsername())).build();
                    } else {
                        logger.info("Credenciales incorrectas para el usuario: {}", usuario.getUsername());
                        return ResponseApp.builder().status(401).message("El usuario o la contraseña no son correctos.").build();
                    }
                } else {
                    logger.info("Usuario inactivo: {}", usuario.getUsername());
                    return ResponseApp.builder().status(404).message("Usuario inactivo").build();
                }
            } else {
                logger.info("Usuario no existe: {}", usuario.getUsername());
                return ResponseApp.builder().status(404).message("Usuario no exite").build();
            }
        } catch (JsonProcessingException e) {
            logger.error("Error al procesar los datos en login del usuario: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al procesar los datos del usuario").build();
        } catch (Exception e) {
            logger.error("Error en el proceso de login: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error en el proceso de login").build();
        }
    }

    /**
     * Desactiva un usuario en la base de datos.
     *
     * @param usuario Objeto Usuario con la información del usuario a desactivar.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp eliminarUsuario(Usuario usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
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
                return ResponseApp.builder().status(200).message("Usuario desactivado exitosamente").build();

            } else {
                logger.info("Usuario no existe para eliminar: {}", usuario.getUsername());
                return ResponseApp.builder().status(404).message("Usuario no exite").build();

            }
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario: {}", e.getMessage());
            return ResponseApp.builder().status(500).message("Error al eliminar el usuario").build();

        }
    }

    /**
     * Suscribe a un usuario a un curso específico.
     *
     * @param userId  ID del usuario a suscribir.
     * @param idCurso ID del curso al que se desea suscribir.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp suscribirCurso(String userId, String idCurso) {
        try {
            // buscar el usuario en la base de datos
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(userId).build());
            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);

                // verificar si el curso ya está en la lista de cursos suscritos
                if (storedUser.getCursos() != null &&
                        storedUser.getCursos().stream().anyMatch(c -> c.getCursoId().equals(idCurso))) {
                    logger.info("El usuario ya está suscrito al curso: {}", idCurso);
                    return ResponseApp.builder().status(200).message("Ya estás suscrito a este curso").build();

                } else {
                    // agregar el curso a la lista de cursos suscritos
                    Curso agregar = cursoProcess.obtenerCursoPorId(idCurso);
                    if (agregar.isActive()) {
                        storedUser.getCursos().add(cursoProcess.obtenerCursoPorId(idCurso));
                    } else {
                        logger.info("Curso inactivo");
                        return ResponseApp.builder().status(404).message("Curso inactivo o inexistente").build();
                    }

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
                    return ResponseApp.builder().status(200).message("Suscripción al curso exitosa").build();
                }
            } else {
                logger.info("Usuario no existe para suscribir al curso: {}", userId);
                return ResponseApp.builder().status(404).message("Usuario no exite").build();
            }
        } catch (Exception e) {
            logger.error("Error al suscribir el curso: {}", e.getMessage());
        }
        return ResponseApp.builder().status(500).message("Accion no permitida").build();
    }

    /**
     * Marca un módulo como visualizado para un usuario.
     *
     * @param userId    ID del usuario que visualizó el módulo.
     * @param verModulo Objeto VerModulo con la información del módulo visualizado.
     * @return Respuesta indicando el resultado de la operación.
     */
    @Override
    public ResponseApp verModulo(String userId, VerModulo verModulo) {
        try {
            boolean update = false;
            Usuario user = getUsuarioPorId(userId);
            for (Curso curso : user.getCursos()) {
                if (curso.getCursoId().equals(verModulo.getCursoId())) {
                    for (Recurso rec : curso.getRecursos()) {
                        if (rec.getId().equals(verModulo.getRecursoId())) {
                            rec.setVisualizado(true);
                            update = true;
                        }
                    }
                }
            }
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(userId).build());

            String json = mapper.writeValueAsString(user);
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .updateExpression("SET perfil = :perfil")
                    .expressionAttributeValues(Map.of(
                            ":perfil", AttributeValue.builder().s(json).build()
                    ))
                    .build();

            client.updateItem(request);
            logger.info("Modulo visualizado correctamente in curso {} recurso {}",
                    verModulo.getCursoId(), verModulo.getRecursoId());
            if (update) {
                return ResponseApp.builder().status(200).message("Módulo visualizado correctamente").build();
            } else {
                return ResponseApp.builder().status(200).message("No estas suscrito a este módulo").build();
            }
        } catch (Exception e) {
            logger.error("Error al obtener el modulo");
        }
        return null;
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario a buscar.
     * @return Objeto Usuario con la información del usuario o null si no se encuentra.
     */
    private Usuario getUsuarioPorId(String userId) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(userId).build());
            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String json = response.item().get(PERFIL).s();
                try {
                    return new ObjectMapper().readValue(json, Usuario.class);
                } catch (Exception e) {
                    logger.error("Error al convertir el JSON a Curso: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error al obtener el usuario por ID");
        }
        return null;
    }
}

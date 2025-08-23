package co.com.capacitanet.dynamodb.service;

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


    private static final String USERNAME = "username";
    private static final String PERFIL = "perfil";
    private static final String TABLE_NAME = "capacitanet_user";

    private final DynamoDbClient client;

    public UsersProcess(DynamoDbClient client) {
        this.client = client;
    }


    @Override
    public String registrarUsuario(Usuario usuario) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            usuario.setPasswordHash(Password.hash(usuario.getPasswordHash()));
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
                storedUser.setPasswordHash(Password.hash(usuario.getPasswordHashNew()));

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
    public String login(Usuario usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(USERNAME, AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName(TABLE_NAME).key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get(PERFIL).s();
                ObjectMapper mapper = new ObjectMapper();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);

                if (Password.verificar(usuario.getPasswordHash(), storedUser.getPasswordHash())) {
                    logger.info("Login exitoso para el usuario: {}", usuario.getUsername());
                    return AuthService.generaJWT(usuario.getUsername());
                } else {
                    logger.info("Credenciales incorrectas para el usuario: {}", usuario.getUsername());
                    return "Credenciales incorrectas";
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
}

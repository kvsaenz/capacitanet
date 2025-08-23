package co.com.capacitanet.dynamodb.service;

import co.com.capacitanet.helpers.Password;
import co.com.capacitanet.model.usuario.Usuario;
import co.com.capacitanet.model.usuario.gateways.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class UsersProcess implements UsuarioRepository {


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
            item.put("username", AttributeValue.builder().s(usuario.getUsername()).build());
            item.put("data", AttributeValue.builder().s(json).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName("capacitanet_user")
                    .item(item)
                    .conditionExpression("attribute_not_exists(username)")
                    .build();

            client.putItem(request);
            return "Usuario registrado exitosamente";
        } catch (JsonProcessingException e) {
            return "Usuario no registrado";
        } catch (ConditionalCheckFailedException e) {
            return "Usuario ya existe";
        } catch (Exception e) {
            return "Usuario no registrado satisfactoriamente";
        }
    }

    @Override
    public String login(Usuario usuario) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("username", AttributeValue.builder().s(usuario.getUsername()).build());

            var response = client.getItem(builder -> builder.tableName("capacitanet_user").key(key));
            if (response.hasItem()) {
                String jsonData = response.item().get("data").s();
                ObjectMapper mapper = new ObjectMapper();
                Usuario storedUser = mapper.readValue(jsonData, Usuario.class);

                if (Password.verificar(usuario.getPasswordHash(), storedUser.getPasswordHash())) {
                    return Password.generaJWT(usuario.getUsername());
                } else {
                    return "Credenciales incorrectas";
                }
            } else {
                return "Usuario no exite";
            }
        } catch (JsonProcessingException e) {
            return "Error al procesar los datos del usuario";
        } catch (Exception e) {
            return "Error en el proceso de login";
        }
    }
}

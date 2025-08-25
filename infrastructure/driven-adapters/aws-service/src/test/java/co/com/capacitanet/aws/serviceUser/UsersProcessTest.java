package co.com.capacitanet.aws.serviceUser;

import co.com.capacitanet.aws.serviceCurso.CursoProcess;
import co.com.capacitanet.model.curso.Curso;
import co.com.capacitanet.model.response.ResponseApp;
import co.com.capacitanet.model.usuario.ChangePassword;
import co.com.capacitanet.model.usuario.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersProcessTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Mock
    private CursoProcess cursoProcess;

    @InjectMocks
    private UsersProcess usersProcess;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("registrarUsuario")
    class RegistrarUsuario {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            Usuario usuario = new Usuario("testuser", "Test", "User", "test@test.com", true, new ArrayList<>(), new ArrayList<>());
            when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());

            ResponseApp response = usersProcess.registrarUsuario(usuario);

            assertEquals(200, response.getStatus());
            assertEquals("Registro exitoso", response.getMessage());
            verify(dynamoDbClient).putItem(any(PutItemRequest.class));
        }

        @Test
        @DisplayName("Should return error when user already exists")
        void shouldReturnErrorWhenUserAlreadyExists() {
            Usuario usuario = new Usuario("testuser", "Test", "User", "test@test.com", true, new ArrayList<>(), new ArrayList<>());
            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenThrow(ConditionalCheckFailedException.builder().build());

            ResponseApp response = usersProcess.registrarUsuario(usuario);

            assertEquals(401, response.getStatus());
            assertEquals("El usuario o correo ya está registrado en el sistema.", response.getMessage());
        }

        @Test
        @DisplayName("Should return error on generic exception")
        void shouldReturnErrorOnGenericException() {
            Usuario usuario = new Usuario("testuser", "Test", "User", "test@test.com", true, new ArrayList<>(), new ArrayList<>());
            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenThrow(new RuntimeException("DynamoDB is down"));

            ResponseApp response = usersProcess.registrarUsuario(usuario);

            assertEquals(500, response.getStatus());
            assertEquals("Usuario no registrado satisfactoriamente", response.getMessage());
        }
    }

    @Nested
    @DisplayName("actualizarUsuario")
    class ActualizarUsuario {

        @Test
        @DisplayName("Should update user password successfully")
        void shouldUpdateUserPasswordSuccessfully() throws JsonProcessingException {
            ChangePassword changePassword = ChangePassword.builder()
                    .password("testuser")
                    .passwordNew("newPassword")
                    .build();
            Usuario storedUser = new Usuario("testuser", "Test", "User", "test@test.com", true, new ArrayList<>(), new ArrayList<>());
            String userJson = mapper.writeValueAsString(storedUser);

            GetItemResponse getItemResponse = GetItemResponse.builder()
                    .item(Map.of("perfil", AttributeValue.builder().s(userJson).build()))
                    .build();

            when(dynamoDbClient.getItem(any(Consumer.class))).thenAnswer(invocation -> {
                Consumer<GetItemRequest.Builder> consumer = invocation.getArgument(0);
                GetItemRequest.Builder builder = GetItemRequest.builder();
                consumer.accept(builder);
                return getItemResponse;
            });
            when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());

            ResponseApp response = usersProcess.actualizarUsuario(changePassword);

            assertEquals(200, response.getStatus());
            assertEquals("Contraseña actualizada exitosamente", response.getMessage());
            verify(dynamoDbClient).updateItem(any(UpdateItemRequest.class));
        }


        @Nested
        @DisplayName("perfilUsuario")
        class PerfilUsuario {

            @Test
            @DisplayName("Should return user profile successfully")
            void shouldReturnUserProfileSuccessfully() throws JsonProcessingException {
                String userId = "testuser";
                Usuario storedUser = new Usuario(userId, "Test", "User", "test@test.com", true, new ArrayList<>(), new ArrayList<>());
                String userJson = mapper.writeValueAsString(storedUser);

                GetItemResponse getItemResponse = GetItemResponse.builder()
                        .item(Map.of("perfil", AttributeValue.builder().s(userJson).build()))
                        .build();

                when(dynamoDbClient.getItem(any(Consumer.class))).thenAnswer(invocation -> {
                    Consumer<GetItemRequest.Builder> consumer = invocation.getArgument(0);
                    GetItemRequest.Builder builder = GetItemRequest.builder();
                    consumer.accept(builder);
                    return getItemResponse;
                });

                ResponseApp response = usersProcess.perfilUsuario(userId);

                assertEquals(200, response.getStatus());
                Usuario responseUser = mapper.readValue(response.getMessage(), Usuario.class);
                assertEquals("********", responseUser.getPassword());
                assertEquals(userId, responseUser.getUsername());
            }

        }


        @Nested
        @DisplayName("eliminarUsuario")
        class EliminarUsuario {

            @Test
            @DisplayName("Should deactivate user successfully")
            void shouldDeactivateUserSuccessfully() throws JsonProcessingException {
                Usuario userToDelete = new Usuario("testuser", null, null, null, false, null, null);
                Usuario storedUser = new Usuario("test@test.com", "Test", "User", "password", true, new ArrayList<>(), new ArrayList<>());
                String userJson = mapper.writeValueAsString(storedUser);

                GetItemResponse getItemResponse = GetItemResponse.builder()
                        .item(Map.of("perfil", AttributeValue.builder().s(userJson).build()))
                        .build();
                when(dynamoDbClient.getItem(any(Consumer.class))).thenAnswer(invocation -> {
                    Consumer<GetItemRequest.Builder> consumer = invocation.getArgument(0);
                    GetItemRequest.Builder builder = GetItemRequest.builder();
                    consumer.accept(builder);
                    return getItemResponse;
                });
                when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());

                ResponseApp response = usersProcess.eliminarUsuario(userToDelete);

                assertEquals(200, response.getStatus());
                assertEquals("Usuario desactivado exitosamente", response.getMessage());
            }
        }

        @Nested
        @DisplayName("suscribirCurso")
        class SuscribirCurso {

            @Test
            @DisplayName("Should subscribe user to an active course successfully")
            void shouldSubscribeUserToCourseSuccessfully() throws JsonProcessingException {
                String userId = "testuser";
                String cursoId = "course1";
                Usuario storedUser = new Usuario(userId, "Test", "test@test.com", "password", true, new ArrayList<>(), new ArrayList<>());
                String userJson = mapper.writeValueAsString(storedUser);
                Curso courseToSubscribe = new Curso();
                courseToSubscribe.setCursoId(cursoId);
                courseToSubscribe.setActive(true);

                GetItemResponse getItemResponse = GetItemResponse.builder()
                        .item(Map.of("perfil", AttributeValue.builder().s(userJson).build()))
                        .build();
                when(dynamoDbClient.getItem(any(Consumer.class))).thenAnswer(invocation -> {
                    Consumer<GetItemRequest.Builder> consumer = invocation.getArgument(0);
                    GetItemRequest.Builder builder = GetItemRequest.builder();
                    consumer.accept(builder);
                    return getItemResponse;
                });
                when(cursoProcess.obtenerCursoPorId(cursoId)).thenReturn(courseToSubscribe);
                when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());

                ResponseApp response = usersProcess.suscribirCurso(userId, cursoId);

                assertEquals(200, response.getStatus());
                assertEquals("Suscripción al curso exitosa", response.getMessage());
            }

            @Test
            @DisplayName("Should return message when user is already subscribed")
            void shouldReturnMessageWhenUserAlreadySubscribed() throws JsonProcessingException {
                String userId = "testuser";
                String cursoId = "course1";
                Curso existingCourse = new Curso();
                existingCourse.setCursoId(cursoId);
                Usuario storedUser = new Usuario(userId, "Test", "test@test.com", "password", true, new ArrayList<>(List.of(existingCourse)), new ArrayList<>());
                String userJson = mapper.writeValueAsString(storedUser);

                GetItemResponse getItemResponse = GetItemResponse.builder()
                        .item(Map.of("perfil", AttributeValue.builder().s(userJson).build()))
                        .build();
                when(dynamoDbClient.getItem(any(Consumer.class))).thenAnswer(invocation -> {
                    Consumer<GetItemRequest.Builder> consumer = invocation.getArgument(0);
                    GetItemRequest.Builder builder = GetItemRequest.builder();
                    consumer.accept(builder);
                    return getItemResponse;
                });

                ResponseApp response = usersProcess.suscribirCurso(userId, cursoId);

                assertEquals(200, response.getStatus());
                assertEquals("Ya estás suscrito a este curso", response.getMessage());
            }
        }
    }
}
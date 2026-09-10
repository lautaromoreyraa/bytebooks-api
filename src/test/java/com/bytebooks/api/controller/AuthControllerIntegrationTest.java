package com.bytebooks.api.controller;

import com.bytebooks.api.config.PruebaDeIntegracion;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Registro, ingreso y salida.
 *
 * Cada test usa su propia IP de origen. No es decoracion: RateLimitFilter corta
 * el registro en 3 por hora por IP, con una cache que vive dos horas y que
 * comparten todas las clases de test, porque el contexto de Spring es el mismo.
 * Sin separar las IP, el cuarto test de la clase empieza a recibir 429 y el
 * fallo aparece en cualquier lado menos donde esta la causa.
 *
 * Sin @Transactional: el filtro JWT resuelve el usuario en su propia unidad de
 * trabajo y con rollback el alta no llega a verse.
 */
@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends PruebaDeIntegracion {

    private static final String PASSWORD = "Local1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiarUsuarios() {
        usuarioRepository.deleteAll();
    }

    /** RateLimitFilter cuenta por getRemoteAddr(), asi que esto aisla presupuestos. */
    private RequestPostProcessor desde(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private String cuerpoDeRegistro(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "nombre", "Camila",
                "apellido", "Sosa",
                "email", email,
                "password", password));
    }

    private String registrar(String email, String ip) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/register")
                        .with(desde(ip))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDeRegistro(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    @DisplayName("al registrarse se devuelve un token y el rol basico")
    void registroDevuelveTokenYRolBasico() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .with(desde("10.0.0.1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDeRegistro("camila@ejemplo.test", PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("camila@ejemplo.test"))
                .andExpect(jsonPath("$.role").value(RolEnum.ROLE_USER.name()));
    }

    @Test
    @DisplayName("el registro nunca devuelve la contrasena ni su hash")
    void registroNoDevuelveElHash() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .with(desde("10.0.0.2"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDeRegistro("camila@ejemplo.test", PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("un email ya registrado da 409 sin confirmar que la cuenta existe")
    void emailRepetidoNoConfirmaLaCuenta() throws Exception {
        registrar("camila@ejemplo.test", "10.0.0.3");

        MvcResult resultado = mockMvc.perform(post("/auth/register")
                        .with(desde("10.0.0.3"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDeRegistro("camila@ejemplo.test", PASSWORD)))
                .andExpect(status().isConflict())
                .andReturn();

        // El mensaje es deliberadamente vago: decir "ese email ya existe"
        // convierte al registro en un oraculo de que cuentas hay.
        assertThat(resultado.getResponse().getContentAsString())
                .doesNotContain("camila@ejemplo.test");
    }

    @Test
    @DisplayName("una contrasena sin mayuscula ni numero se rechaza con el motivo")
    void contrasenaDebilSeRechaza() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .with(desde("10.0.0.4"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDeRegistro("camila@ejemplo.test", "todominuscula")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("un email con formato invalido se rechaza")
    void emailInvalidoSeRechaza() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .with(desde("10.0.0.5"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDeRegistro("no-es-un-email", PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("el cuarto registro desde la misma IP se corta con 429")
    void elRegistroTieneLimitePorIp() throws Exception {
        // Un alta masiva de cuentas desde una sola IP es el abuso obvio de este
        // endpoint. El limite es 3 por hora.
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/auth/register")
                            .with(desde("10.0.0.6"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cuerpoDeRegistro("cuenta" + i + "@ejemplo.test", PASSWORD)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/auth/register")
                        .with(desde("10.0.0.6"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDeRegistro("cuenta4@ejemplo.test", PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    @DisplayName("con las credenciales correctas se entra")
    void loginCorrecto() throws Exception {
        registrar("camila@ejemplo.test", "10.0.0.7");

        mockMvc.perform(post("/auth/login")
                        .with(desde("10.0.0.7"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "camila@ejemplo.test",
                                "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("el token deja de servir despues de salir")
    void elTokenMuereConElLogout() throws Exception {
        String token = registrar("camila@ejemplo.test", "10.0.0.8");

        mockMvc.perform(get("/usuarios/me/favoritos")
                        .with(desde("10.0.0.8"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/logout")
                        .with(desde("10.0.0.8"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful());

        // Sin la lista negra, cerrar sesion seria solo borrar el token del
        // navegador: quien lo hubiera copiado antes lo seguiria usando.
        mockMvc.perform(get("/usuarios/me/favoritos")
                        .with(desde("10.0.0.8"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("una contrasena incorrecta responde 401, no 500")
    void contrasenaIncorrectaDa401() throws Exception {
        // ROJO A PROPOSITO — punto 22 de REVISION.md. AuthServiceImpl lanza
        // IllegalArgumentException y GlobalExceptionHandler no la contempla, asi
        // que cae en el generico. Es el camino de error mas transitado de la app.
        registrar("camila@ejemplo.test", "10.0.0.9");

        mockMvc.perform(post("/auth/login")
                        .with(desde("10.0.0.9"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "camila@ejemplo.test",
                                "password", "OtraContrasena1"))))
                .andExpect(status().isUnauthorized());
    }
}

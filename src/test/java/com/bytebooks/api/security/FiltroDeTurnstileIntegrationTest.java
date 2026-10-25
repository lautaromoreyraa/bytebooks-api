package com.bytebooks.api.security;

import com.bytebooks.api.client.TurnstileClient;
import com.bytebooks.api.config.PruebaDeIntegracion;
import com.bytebooks.api.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El registro con Turnstile encendido. El cliente se simula: lo que se prueba
 * aca es que el filtro corta antes de crear la cuenta y que el 403 le llega al
 * navegador con CORS. La conversacion con Cloudflare la cubre TurnstileClientTest.
 */
@AutoConfigureMockMvc
class FiltroDeTurnstileIntegrationTest extends PruebaDeIntegracion {

    private static final String ORIGEN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TurnstileClient turnstileClient;

    @BeforeEach
    void limpiarUsuarios() {
        usuarioRepository.deleteAll();
    }

    private String cuerpo(String email) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "nombre", "Camila", "apellido", "Sosa", "email", email, "password", "Local1234"));
    }

    @Test
    @DisplayName("Sin pasar el desafio el registro da 403, con CORS, y no crea la cuenta")
    void sinDesafioNoSeCreaLaCuenta() throws Exception {
        when(turnstileClient.valido(any(), any())).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                        .header("Origin", ORIGEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("script@test.com")))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN))
                .andExpect(jsonPath("$.message").exists());

        assertThat(usuarioRepository.existsByEmail("script@test.com")).isFalse();
    }

    @Test
    @DisplayName("Con el desafio resuelto se crea la cuenta y el token llega al cliente")
    void conDesafioSeCreaLaCuenta() throws Exception {
        when(turnstileClient.valido(eq("tok-123"), any())).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .header(FiltroDeTurnstile.CABECERA_TOKEN, "tok-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("persona@test.com")))
                .andExpect(status().isCreated());

        assertThat(usuarioRepository.existsByEmail("persona@test.com")).isTrue();
    }

    @Test
    @DisplayName("El ingreso no pide desafio: el limite de intentos ya lo cubre")
    void elIngresoNoPideDesafio() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie@test.com\",\"password\":\"Local1234\"}"))
                .andExpect(status().isUnauthorized());

        verify(turnstileClient, never()).valido(any(), any());
    }
}

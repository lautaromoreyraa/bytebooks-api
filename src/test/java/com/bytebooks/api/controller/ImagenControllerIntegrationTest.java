package com.bytebooks.api.controller;

import com.bytebooks.api.config.PruebaDeIntegracion;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La firma de subidas reemplaza a un upload preset abierto: tiene que pedir
 * sesion, avisar con 503 si falta configuracion y firmar exactamente lo que
 * Cloudinary va a verificar.
 */
class ImagenControllerIntegrationTest {

    private static RequestPostProcessor conSesion() {
        return authentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Nested
    @AutoConfigureMockMvc
    class SinConfiguracion extends PruebaDeIntegracion {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("sin sesion no se firma nada")
        void exigeSesion() throws Exception {
            mockMvc.perform(post("/imagenes/firma")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("sin credenciales de Cloudinary responde 503, no 500")
        void sinCredencialesDa503() throws Exception {
            mockMvc.perform(post("/imagenes/firma").with(conSesion()))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Nested
    @AutoConfigureMockMvc
    @TestPropertySource(properties = {
            "cloudinary.cloud-name=nube-de-test",
            "cloudinary.api-key=clave-publica",
            "cloudinary.api-secret=secreto-de-test",
            "cloudinary.carpeta=bytebooks-test"
    })
    class ConConfiguracion extends PruebaDeIntegracion {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("firma carpeta y hora como las verifica Cloudinary, sin exponer el secreto")
        void firmaCarpetaYHora() throws Exception {
            String cuerpo = mockMvc.perform(post("/imagenes/firma").with(conSesion()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(cuerpo).doesNotContain("secreto-de-test");
            JsonNode firma = objectMapper.readTree(cuerpo);
            assertThat(firma.get("apiKey").asText()).isEqualTo("clave-publica");
            assertThat(firma.get("nombreDeLaNube").asText()).isEqualTo("nube-de-test");
            assertThat(firma.get("carpeta").asText()).isEqualTo("bytebooks-test");

            long marcaDeTiempo = firma.get("marcaDeTiempo").asLong();
            String esperada = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(
                    ("folder=bytebooks-test&timestamp=" + marcaDeTiempo + "secreto-de-test")
                            .getBytes(StandardCharsets.UTF_8)));
            assertThat(firma.get("firma").asText()).isEqualTo(esperada);
        }
    }
}

package com.bytebooks.api.security;

import com.bytebooks.api.config.PruebaDeIntegracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Solo los origenes exactos de CORS_ORIGENES; en los tests, el default de desarrollo. */
@AutoConfigureMockMvc
class CorsIntegrationTest extends PruebaDeIntegracion {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("el origen configurado pasa el preflight")
    void origenConfigurado() throws Exception {
        mockMvc.perform(options("/libros").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("un proyecto de Vercel con un nombre parecido ya no pasa")
    void vercelParecido() throws Exception {
        mockMvc.perform(options("/libros").header("Origin", "https://bytebooks-frontend-otro.vercel.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("otro puerto de localhost tampoco")
    void otroPuerto() throws Exception {
        mockMvc.perform(options("/libros").header("Origin", "http://localhost:9999")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}

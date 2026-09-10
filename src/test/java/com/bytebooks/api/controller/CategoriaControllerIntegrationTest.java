package com.bytebooks.api.controller;

import com.bytebooks.api.config.PruebaDeIntegracion;
import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.repository.CategoriaRepository;
import com.bytebooks.api.repository.LibroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las categorias se leen sin sesion y solo las toca administracion. Es el unico
 * recurso donde moderador no alcanza, asi que conviene que quede fijado.
 */
@AutoConfigureMockMvc
class CategoriaControllerIntegrationTest extends PruebaDeIntegracion {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiar() {
        libroRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    private RequestPostProcessor comoRol(RolEnum rol) {
        return authentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                null,
                List.of(new SimpleGrantedAuthority(rol.name()))));
    }

    private Categoria guardar(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    private String cuerpo(String nombre) throws Exception {
        return objectMapper.writeValueAsString(Map.of("nombre", nombre));
    }

    @Test
    @DisplayName("el listado se lee sin iniciar sesion")
    void listadoPublico() throws Exception {
        guardar("Narrativa");

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Narrativa"));
    }

    @Test
    @DisplayName("una categoria inexistente da 404")
    void inexistenteDa404() throws Exception {
        mockMvc.perform(get("/categorias/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("crear exige sesion")
    void crearExigeSesion() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Ensayo")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un moderador no puede tocar las categorias: son de admin")
    void moderadorNoPuede() throws Exception {
        Categoria existente = guardar("Narrativa");

        mockMvc.perform(post("/categorias")
                        .with(comoRol(RolEnum.ROLE_MODERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Ensayo")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/categorias/{id}", existente.getId())
                        .with(comoRol(RolEnum.ROLE_MODERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Otra cosa")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/categorias/{id}", existente.getId())
                        .with(comoRol(RolEnum.ROLE_MODERATOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un admin crea, edita y elimina")
    void elAdminGestiona() throws Exception {
        mockMvc.perform(post("/categorias")
                        .with(comoRol(RolEnum.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Ensayo")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ensayo"));

        Categoria creada = categoriaRepository.findAll().get(0);

        mockMvc.perform(put("/categorias/{id}", creada.getId())
                        .with(comoRol(RolEnum.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Ensayo y cronica")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ensayo y cronica"));

        mockMvc.perform(delete("/categorias/{id}", creada.getId())
                        .with(comoRol(RolEnum.ROLE_ADMIN)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categorias"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("una categoria sin nombre se rechaza")
    void nombreObligatorio() throws Exception {
        mockMvc.perform(post("/categorias")
                        .with(comoRol(RolEnum.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nombre", ""))))
                .andExpect(status().isBadRequest());
    }
}

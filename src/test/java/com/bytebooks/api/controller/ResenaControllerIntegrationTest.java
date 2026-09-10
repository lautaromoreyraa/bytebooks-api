package com.bytebooks.api.controller;

import com.bytebooks.api.config.PruebaDeIntegracion;
import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.fixture.FabricaDeLibros;
import com.bytebooks.api.fixture.FabricaDeUsuarios;
import com.bytebooks.api.repository.CategoriaRepository;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.repository.ResenaRepository;
import com.bytebooks.api.repository.UsuarioRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Publicar, listar y borrar resenas. Lo mas delicado es quien puede borrar la
 * de otro, porque la regla no esta en SecurityConfig sino adentro del service.
 */
@AutoConfigureMockMvc
class ResenaControllerIntegrationTest extends PruebaDeIntegracion {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Libro libro;
    private Usuario autora;

    @BeforeEach
    void prepararDatos() {
        resenaRepository.deleteAll();
        usuarioRepository.deleteAll();
        libroRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria categoria = new Categoria();
        categoria.setNombre("Narrativa");
        categoria = categoriaRepository.save(categoria);

        Libro nuevo = FabricaDeLibros.disponible("Rayuela");
        nuevo.setId(null);
        nuevo.setCategorias(Set.of(categoria));
        libro = libroRepository.save(nuevo);

        autora = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER));
    }

    private RequestPostProcessor como(Usuario usuario) {
        return authentication(new UsernamePasswordAuthenticationToken(
                usuario.getId().toString(),
                null,
                List.of(new SimpleGrantedAuthority(usuario.getRol().name()))));
    }

    private String cuerpo(String comentario, Integer puntuacion) throws Exception {
        return objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
            put("comentario", comentario);
            put("puntuacion", puntuacion);
        }});
    }

    private UUID publicar(Usuario usuario, String comentario, int puntuacion) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/libros/{id}/resenas", libro.getId())
                        .with(como(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(comentario, puntuacion)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("id").asText());
    }

    @Test
    @DisplayName("publicar una resena exige sesion")
    void publicarExigeSesion() throws Exception {
        mockMvc.perform(post("/libros/{id}/resenas", libro.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Muy buena", 5)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("la resena publicada vuelve con el nombre de quien la escribio")
    void resenaPublicadaTraeElNombre() throws Exception {
        mockMvc.perform(post("/libros/{id}/resenas", libro.getId())
                        .with(como(autora))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("La lei dos veces", 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comentario").value("La lei dos veces"))
                .andExpect(jsonPath("$.puntuacion").value(5))
                .andExpect(jsonPath("$.nombreUsuario").isNotEmpty())
                .andExpect(jsonPath("$.usuarioId").value(autora.getId().toString()));
    }

    @Test
    @DisplayName("se puede puntuar sin escribir comentario")
    void comentarioOpcional() throws Exception {
        mockMvc.perform(post("/libros/{id}/resenas", libro.getId())
                        .with(como(autora))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(null, 4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puntuacion").value(4));
    }

    @Test
    @DisplayName("una puntuacion fuera de 1..5 se rechaza")
    void puntuacionFueraDeRango() throws Exception {
        for (int invalida : new int[]{0, 6}) {
            mockMvc.perform(post("/libros/{id}/resenas", libro.getId())
                            .with(como(autora))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cuerpo("Comentario", invalida)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.puntuacion").exists());
        }
    }

    @Test
    @DisplayName("un comentario de mas de 2000 caracteres se rechaza")
    void comentarioDemasiadoLargo() throws Exception {
        mockMvc.perform(post("/libros/{id}/resenas", libro.getId())
                        .with(como(autora))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("x".repeat(2001), 3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comentario").exists());
    }

    @Test
    @DisplayName("el listado es publico y trae el total real, no el de la pagina")
    void listadoPublicoConTotal() throws Exception {
        publicar(autora, "Una", 5);
        publicar(usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER)), "Otra", 3);

        mockMvc.perform(get("/libros/{id}/resenas", libro.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("cada quien puede borrar su propia resena")
    void borrarLaPropia() throws Exception {
        UUID resena = publicar(autora, "Me arrepenti", 2);

        mockMvc.perform(delete("/resenas/{id}", resena).with(como(autora)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/libros/{id}/resenas", libro.getId()))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("un usuario comun no puede borrar la resena de otro")
    void noSePuedeBorrarLaAjena() throws Exception {
        UUID resena = publicar(autora, "Mia", 5);
        Usuario intrusa = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER));

        mockMvc.perform(delete("/resenas/{id}", resena).with(como(intrusa)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un moderador si puede borrar la resena de otro")
    void elModeradorModera() throws Exception {
        UUID resena = publicar(autora, "Mia", 5);
        Usuario moderador = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_MODERATOR));

        mockMvc.perform(delete("/resenas/{id}", resena).with(como(moderador)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("resenar un libro que no existe da 404")
    void resenarLibroInexistente() throws Exception {
        mockMvc.perform(post("/libros/{id}/resenas", UUID.randomUUID())
                        .with(como(autora))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Comentario", 4)))
                .andExpect(status().isNotFound());
    }
}

package com.bytebooks.api.controller;

import com.bytebooks.api.config.PruebaDeIntegracion;
import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.fixture.FabricaDeLibros;
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
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorre el camino completo —HTTP, seguridad, service, JPA, MySQL— porque lo que
 * se prueba aca depende de las reglas de SecurityConfig y de las restricciones
 * de la base, y ninguna de las dos existe en un test unitario.
 */
// Sin @Transactional a proposito: con rollback el insert no se descarga a la
// base y las restricciones UNIQUE nunca se disparan. El aislamiento lo da el
// deleteAll() de @BeforeEach.
@AutoConfigureMockMvc
class LibroControllerIntegrationTest extends PruebaDeIntegracion {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Categoria categoria;

    @BeforeEach
    void prepararCatalogo() {
        libroRepository.deleteAll();
        categoriaRepository.deleteAll();

        categoria = new Categoria();
        categoria.setNombre("Narrativa");
        categoria = categoriaRepository.save(categoria);
    }

    /**
     * El principal es un String con el id: asi lo espera
     * {@code @AuthenticationPrincipal String userId} en los controllers.
     */
    private RequestPostProcessor comoRol(RolEnum rol) {
        return authentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                null,
                List.of(new SimpleGrantedAuthority(rol.name()))));
    }

    private Libro guardar(Libro libro) {
        libro.setId(null);
        libro.setCategorias(Set.of(categoria));
        return libroRepository.save(libro);
    }

    @Test
    @DisplayName("el catalogo publico no lista los libros ocultos")
    void catalogoPublicoSinOcultos() throws Exception {
        guardar(FabricaDeLibros.disponible("A la vista"));
        guardar(FabricaDeLibros.oculto("Fuera de vista"));

        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("A la vista"));
    }

    @Test
    @DisplayName("un admin si los lista, porque tiene que poder editarlos")
    void adminListaLosOcultos() throws Exception {
        guardar(FabricaDeLibros.disponible("A la vista"));
        guardar(FabricaDeLibros.oculto("Fuera de vista"));

        mockMvc.perform(get("/libros").with(comoRol(RolEnum.ROLE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("pedir un oculto por id da 404 y no 403, para no delatar que existe")
    void ocultoPorIdDa404() throws Exception {
        Libro oculto = guardar(FabricaDeLibros.oculto("Fuera de vista"));

        mockMvc.perform(get("/libros/{id}", oculto.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("el isbn viaja en la respuesta")
    void elIsbnViajaEnLaRespuesta() throws Exception {
        Libro libro = FabricaDeLibros.disponible("Con isbn");
        libro.setIsbn("9788437604947");
        guardar(libro);

        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("9788437604947"));
    }

    @Test
    @DisplayName("un moderador no puede crear libros: eso es solo de admin")
    void moderadorNoPuedeCrear() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "titulo", "Nuevo",
                "autor", "Autora",
                "categoriaIds", List.of(categoria.getId())));

        mockMvc.perform(post("/libros")
                        .with(comoRol(RolEnum.ROLE_MODERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("al crear se persisten editorial, anio, portada y estado")
    void alCrearSePersistenLosCampos() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "titulo", "Cien anios de soledad",
                "autor", "Gabriel Garcia Marquez",
                "categoriaIds", List.of(categoria.getId()),
                "editorial", "Sudamericana",
                "anioPublicacion", "1967",
                "portada", "https://ejemplo.test/tapa.jpg",
                "estadoLibro", "OCULTO"));

        mockMvc.perform(post("/libros")
                        .with(comoRol(RolEnum.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.editorial").value("Sudamericana"))
                .andExpect(jsonPath("$.anioPublicacion").value("1967"))
                .andExpect(jsonPath("$.portada").value("https://ejemplo.test/tapa.jpg"))
                .andExpect(jsonPath("$.estadoLibro").value("OCULTO"));
    }

    @Test
    @DisplayName("un anio que no son cuatro digitos se rechaza con 400")
    void anioInvalidoDa400() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "titulo", "Con anio malo",
                "autor", "Autora",
                "categoriaIds", List.of(categoria.getId()),
                "anioPublicacion", "19a4"));

        mockMvc.perform(post("/libros")
                        .with(comoRol(RolEnum.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.anioPublicacion").exists());
    }

    @Test
    @DisplayName("un titulo repetido responde 409, no 500")
    void tituloRepetidoDa409() throws Exception {
        // ROJO A PROPOSITO — punto 7 de REVISION.md. Hoy la violacion de la
        // restriccion UNIQUE cae en el handler generico y sale como 500.
        guardar(FabricaDeLibros.disponible("Titulo repetido"));

        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "titulo", "Titulo repetido",
                "autor", "Otra autora",
                "categoriaIds", List.of(categoria.getId())));

        mockMvc.perform(post("/libros")
                        .with(comoRol(RolEnum.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict());
    }
}

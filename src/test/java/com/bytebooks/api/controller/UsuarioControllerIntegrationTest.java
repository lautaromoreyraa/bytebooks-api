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
import com.bytebooks.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El perfil publico expone los favoritos de cualquiera sin autenticacion, asi
 * que es la otra puerta por la que puede escaparse un libro oculto.
 */
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerIntegrationTest extends PruebaDeIntegracion {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Categoria categoria;

    @BeforeEach
    void prepararDatos() {
        usuarioRepository.deleteAll();
        libroRepository.deleteAll();
        categoriaRepository.deleteAll();

        /*
         * El flush no es decorativo. Esta clase es @Transactional, asi que nada
         * llega a la base hasta que algo lo obligue, y al vaciar la sesion
         * Hibernate manda primero los INSERT y despues los DELETE. Las clases
         * que no son transaccionales dejan su propia categoria "Narrativa"
         * cargada, y el nombre es UNIQUE: sin este flush, el insert de abajo
         * choca contra una fila que el deleteAll todavia no borro y la peticion
         * del test responde 409.
         */
        categoriaRepository.flush();

        categoria = new Categoria();
        categoria.setNombre("Narrativa");
        categoria = categoriaRepository.save(categoria);
    }

    private Libro guardar(Libro libro) {
        libro.setId(null);
        libro.setCategorias(Set.of(categoria));
        return libroRepository.save(libro);
    }

    @Test
    @DisplayName("los favoritos publicos no exponen los libros ocultos")
    void favoritosPublicosSinOcultos() throws Exception {
        Libro visible = guardar(FabricaDeLibros.disponible("A la vista"));
        Libro oculto = guardar(FabricaDeLibros.oculto("Fuera de vista"));

        Usuario usuaria = usuarioRepository.save(
                FabricaDeUsuarios.conFavoritos(RolEnum.ROLE_USER, List.of(visible, oculto)));

        mockMvc.perform(get("/usuarios/{id}/favoritos", usuaria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("A la vista"));
    }

    @Test
    @DisplayName("el perfil publico se puede leer sin iniciar sesion")
    void perfilPublicoSinSesion() throws Exception {
        Usuario usuaria = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER));

        mockMvc.perform(get("/usuarios/{id}", usuaria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre"));
    }

    @Test
    @DisplayName("el perfil publico no expone el email ni el hash de la clave")
    void perfilPublicoNoExponeDatosSensibles() throws Exception {
        Usuario usuaria = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER));

        mockMvc.perform(get("/usuarios/{id}", usuaria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.rol").doesNotExist());
    }

    @Test
    @DisplayName("mis favoritos exige sesion")
    void misFavoritosExigeSesion() throws Exception {
        mockMvc.perform(get("/usuarios/me/favoritos"))
                .andExpect(status().isUnauthorized());
    }
}

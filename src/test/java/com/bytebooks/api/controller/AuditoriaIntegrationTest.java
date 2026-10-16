package com.bytebooks.api.controller;

import com.bytebooks.api.config.PruebaDeIntegracion;
import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.domain.Resena;
import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.fixture.FabricaDeLibros;
import com.bytebooks.api.fixture.FabricaDeUsuarios;
import com.bytebooks.api.repository.CategoriaRepository;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.repository.ResenaRepository;
import com.bytebooks.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los errores que encontro la auditoria de codigo del 2026-10-16, cada uno con
 * el caso que lo reproduce.
 */
@AutoConfigureMockMvc
class AuditoriaIntegrationTest extends PruebaDeIntegracion {

    @Autowired private MockMvc mockMvc;
    @Autowired private LibroRepository libroRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ResenaRepository resenaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JdbcTemplate jdbc;

    private Categoria categoria;

    @BeforeEach
    void limpiar() {
        resenaRepository.deleteAll();
        jdbc.update("DELETE FROM usuario_libros_guardados");
        usuarioRepository.deleteAll();
        libroRepository.deleteAll();
        categoriaRepository.deleteAll();
        categoria = new Categoria();
        categoria.setNombre("Narrativa");
        categoria = categoriaRepository.save(categoria);
    }

    private RequestPostProcessor como(Usuario usuario) {
        return authentication(new UsernamePasswordAuthenticationToken(
                usuario.getId().toString(), null, List.of(new SimpleGrantedAuthority(usuario.getRol().name()))));
    }

    private Libro libro(String titulo) {
        Libro libro = FabricaDeLibros.disponible(titulo);
        libro.setId(null);
        libro.setCategorias(Set.of(categoria));
        return libroRepository.save(libro);
    }

    @Test
    @DisplayName("se puede borrar un libro que tiene resenas y que alguien guardo en favoritos")
    void borrarLibroConResenasYFavoritos() throws Exception {
        Libro libro = libro("Con historia");
        Usuario lectora = usuarioRepository.save(FabricaDeUsuarios.conFavoritos(RolEnum.ROLE_USER, List.of(libro)));
        Resena resena = new Resena();
        resena.setLibro(libro);
        resena.setUsuario(lectora);
        resena.setPuntuacion(4);
        resena.setFechaResena(new Date());
        resenaRepository.save(resena);
        Usuario moderador = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_MODERATOR));

        mockMvc.perform(delete("/libros/{id}", libro.getId()).with(como(moderador)))
                .andExpect(status().isNoContent());

        assertThat(libroRepository.existsById(libro.getId())).isFalse();
        assertThat(resenaRepository.countByLibroId(libro.getId())).isZero();
        mockMvc.perform(get("/usuarios/{id}/favoritos", lectora.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("un JSON mal formado es un 400, no un error interno")
    void jsonMalFormado() throws Exception {
        Usuario usuario = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER));
        mockMvc.perform(put("/usuarios/me/perfil").with(como(usuario))
                        .contentType(MediaType.APPLICATION_JSON).content("{ esto no es json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("un id que no es UUID es un 400, no un error interno")
    void idQueNoEsUuid() throws Exception {
        mockMvc.perform(get("/libros/no-es-un-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("un metodo que la ruta no acepta es un 405")
    void metodoNoPermitido() throws Exception {
        Usuario admin = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_ADMIN));
        mockMvc.perform(post("/libros/{id}", UUID.randomUUID()).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("una ruta que no existe es un 404 para quien tiene sesion")
    void rutaInexistente() throws Exception {
        Usuario usuario = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER));
        mockMvc.perform(get("/no-existe").with(como(usuario))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("falta un parametro obligatorio: 400")
    void parametroFaltante() throws Exception {
        Usuario admin = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_ADMIN));
        mockMvc.perform(get("/admin/libros/import/previsualizar").with(como(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("la foto de perfil sólo puede ser una imagen subida a Cloudinary")
    void fotoDePerfilSoloDeCloudinary() throws Exception {
        Usuario usuario = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_USER));
        mockMvc.perform(put("/usuarios/me/perfil").with(como(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fotoPerfil\":\"https://rastreador.example/pixel.gif\",\"descripcion\":\"hola\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/usuarios/me/perfil").with(como(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fotoPerfil\":\"https://res.cloudinary.com/nube/image/upload/v1/bytebooks/a.jpg\",\"descripcion\":\"hola\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("la portada de un libro tiene que ser una URL https")
    void portadaHttps() throws Exception {
        Usuario admin = usuarioRepository.save(FabricaDeUsuarios.con(RolEnum.ROLE_ADMIN));
        String cuerpo = "{\"titulo\":\"Nuevo\",\"autor\":\"Alguien\",\"categoriaIds\":[\"" + categoria.getId()
                + "\"],\"portada\":\"javascript:alert(1)\"}";
        mockMvc.perform(post("/libros").with(como(admin)).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }
}

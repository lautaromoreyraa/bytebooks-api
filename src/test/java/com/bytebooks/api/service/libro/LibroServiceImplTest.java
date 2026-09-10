package com.bytebooks.api.service.libro;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.libro.LibroRequestDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.fixture.FabricaDeLibros;
import com.bytebooks.api.mapper.libro.LibroMapper;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.service.categoria.CategoriaService;
import com.bytebooks.api.service.libro.Impl.LibroServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * La visibilidad de los libros ocultos y el guardado de los campos del
 * formulario viven en este service, asi que se prueban aca sin levantar Spring.
 * El rol se lee del SecurityContextHolder, que se arma a mano en cada caso.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LibroServiceImplTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private LibroMapper libroMapper;

    @InjectMocks
    private LibroServiceImpl servicio;

    @Captor
    private ArgumentCaptor<Libro> libroGuardado;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(RolEnum rol) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        UUID.randomUUID().toString(),
                        null,
                        List.of(new SimpleGrantedAuthority(rol.name()))));
    }

    private void dejarSinAutenticar() {
        SecurityContextHolder.clearContext();
    }

    /** El mapper real se prueba aparte; aca solo hace falta identificar el libro. */
    private void mapearPorTitulo() {
        when(libroMapper.toResponseDto(any(Libro.class))).thenAnswer(invocacion -> {
            Libro libro = invocacion.getArgument(0);
            return new LibroResponseDto(
                    libro.getId(), libro.getIsbn(), libro.getTitulo(), libro.getAutor(),
                    libro.getDescripcion(), List.of(), libro.getEditorial(),
                    libro.getAnioPublicacion(), libro.getPortada(), libro.getEstadoLibro());
        });
    }

    @Nested
    @DisplayName("Visibilidad de los libros ocultos")
    class Visibilidad {

        @Test
        @DisplayName("el catalogo publico no incluye los ocultos")
        void catalogoPublicoSinOcultos() {
            dejarSinAutenticar();
            mapearPorTitulo();
            when(libroRepository.findAll()).thenReturn(List.of(
                    FabricaDeLibros.disponible("Visible"),
                    FabricaDeLibros.oculto("Escondido")));

            List<LibroResponseDto> resultado = servicio.getAllLibros();

            assertThat(resultado).extracting(LibroResponseDto::titulo)
                    .containsExactly("Visible");
        }

        @Test
        @DisplayName("un usuario comun tampoco ve los ocultos")
        void usuarioComunSinOcultos() {
            autenticarComo(RolEnum.ROLE_USER);
            mapearPorTitulo();
            when(libroRepository.findAll()).thenReturn(List.of(
                    FabricaDeLibros.disponible("Visible"),
                    FabricaDeLibros.oculto("Escondido")));

            assertThat(servicio.getAllLibros()).extracting(LibroResponseDto::titulo)
                    .containsExactly("Visible");
        }

        @Test
        @DisplayName("admin y moderador si los ven, porque necesitan editarlos")
        void gestoresVenLosOcultos() {
            mapearPorTitulo();
            when(libroRepository.findAll()).thenReturn(List.of(
                    FabricaDeLibros.disponible("Visible"),
                    FabricaDeLibros.oculto("Escondido")));

            for (RolEnum rol : List.of(RolEnum.ROLE_ADMIN, RolEnum.ROLE_MODERATOR)) {
                autenticarComo(rol);
                assertThat(servicio.getAllLibros())
                        .as("rol %s", rol)
                        .extracting(LibroResponseDto::titulo)
                        .containsExactlyInAnyOrder("Visible", "Escondido");
            }
        }

        @Test
        @DisplayName("pedir un oculto por id responde como si no existiera")
        void ocultoPorIdSeComportaComoInexistente() {
            dejarSinAutenticar();
            Libro oculto = FabricaDeLibros.oculto("Escondido");
            when(libroRepository.findById(oculto.getId())).thenReturn(Optional.of(oculto));

            assertThatThrownBy(() -> servicio.getLibroById(oculto.getId()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining(oculto.getId().toString());
        }

        @Test
        @DisplayName("un gestor si puede pedir un oculto por id")
        void gestorPuedePedirUnOculto() {
            autenticarComo(RolEnum.ROLE_MODERATOR);
            mapearPorTitulo();
            Libro oculto = FabricaDeLibros.oculto("Escondido");
            when(libroRepository.findById(oculto.getId())).thenReturn(Optional.of(oculto));

            assertThat(servicio.getLibroById(oculto.getId()).titulo()).isEqualTo("Escondido");
        }
    }

    @Nested
    @DisplayName("Campos que llegan del formulario")
    class CamposDelFormulario {

        private LibroRequestDto pedido(String editorial, String anio, String portada,
                                       EstadoLibroEnum estado) {
            return new LibroRequestDto("Un titulo", "Una autora", "Una sinopsis",
                    List.of(UUID.randomUUID()), editorial, anio, portada, estado);
        }

        private void prepararGuardado() {
            Categoria categoria = FabricaDeLibros.categoria("Narrativa");
            when(categoriaService.getCategoriaEntitiesByIds(anyList()))
                    .thenReturn(Set.of(categoria));
            when(libroRepository.save(any(Libro.class)))
                    .thenAnswer(invocacion -> invocacion.getArgument(0));
            mapearPorTitulo();
        }

        @Test
        @DisplayName("al crear se guardan editorial, anio, portada y estado")
        void alCrearSeGuardanTodos() {
            prepararGuardado();

            servicio.agregarLibro(pedido("Sudamericana", "1967",
                    "https://ejemplo.test/tapa.jpg", EstadoLibroEnum.OCULTO));

            org.mockito.Mockito.verify(libroRepository).save(libroGuardado.capture());
            Libro guardado = libroGuardado.getValue();
            assertThat(guardado.getEditorial()).isEqualTo("Sudamericana");
            assertThat(guardado.getAnioPublicacion()).isEqualTo("1967");
            assertThat(guardado.getPortada()).isEqualTo("https://ejemplo.test/tapa.jpg");
            assertThat(guardado.getEstadoLibro()).isEqualTo(EstadoLibroEnum.OCULTO);
        }

        @Test
        @DisplayName("sin estado explicito el libro nace disponible")
        void sinEstadoNaceDisponible() {
            prepararGuardado();

            servicio.agregarLibro(pedido("Sudamericana", "1967", null, null));

            org.mockito.Mockito.verify(libroRepository).save(libroGuardado.capture());
            assertThat(libroGuardado.getValue().getEstadoLibro())
                    .isEqualTo(EstadoLibroEnum.DISPONIBLE);
        }

        @Test
        @DisplayName("al editar tambien se guardan, no solo al crear")
        void alEditarTambienSeGuardan() {
            Libro existente = FabricaDeLibros.disponible("Titulo viejo");
            when(libroRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
            prepararGuardado();

            servicio.actualizarLibro(existente.getId(),
                    pedido("Minotauro", "1954", "https://ejemplo.test/otra.jpg",
                            EstadoLibroEnum.OCULTO));

            org.mockito.Mockito.verify(libroRepository).save(libroGuardado.capture());
            Libro guardado = libroGuardado.getValue();
            assertThat(guardado.getEditorial()).isEqualTo("Minotauro");
            assertThat(guardado.getAnioPublicacion()).isEqualTo("1954");
            assertThat(guardado.getEstadoLibro()).isEqualTo(EstadoLibroEnum.OCULTO);
        }

        @Test
        @DisplayName("un anio vacio se guarda como null, no como cadena vacia")
        void anioVacioSeGuardaNull() {
            prepararGuardado();

            servicio.agregarLibro(pedido("Sudamericana", "", null, null));

            org.mockito.Mockito.verify(libroRepository).save(libroGuardado.capture());
            assertThat(libroGuardado.getValue().getAnioPublicacion()).isNull();
        }

        @Test
        @DisplayName("una editorial y una portada vacias tambien se guardan como null")
        void editorialYPortadaVaciasSeGuardanNull() {
            // ROJO A PROPOSITO — punto 19 de REVISION.md. Hoy normalizar() se
            // aplica solo a anioPublicacion, asi que estos dos quedan como "".
            prepararGuardado();

            servicio.agregarLibro(pedido("", "1967", "", null));

            org.mockito.Mockito.verify(libroRepository).save(libroGuardado.capture());
            Libro guardado = libroGuardado.getValue();
            assertThat(guardado.getEditorial()).isNull();
            assertThat(guardado.getPortada()).isNull();
        }
    }
}

package com.bytebooks.api.mapper.libro;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.categoria.CategoriaResponseDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.fixture.FabricaDeLibros;
import com.bytebooks.api.mapper.categoria.CategoriaMapper;
import com.bytebooks.api.mapper.libro.Impl.LibroMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El mapper decide que se expone y que no, asi que se prueba con la
 * implementacion real y no con un mock: justamente lo que se quiere verificar es
 * el contenido del DTO.
 */
class LibroMapperImplTest {

    private LibroMapper mapper;

    @BeforeEach
    void prepararMapper() {
        CategoriaMapper categoriaMapper = new CategoriaMapper() {
            @Override
            public Categoria toCategoria(com.bytebooks.api.dto.categoria.CategoriaRequestDto dto) {
                throw new UnsupportedOperationException("no se usa en este test");
            }

            @Override
            public CategoriaResponseDto toResponseDto(Categoria categoria) {
                return new CategoriaResponseDto(categoria.getId(), categoria.getNombre(),
                        categoria.getDescripcion());
            }
        };
        mapper = new LibroMapperImpl(categoriaMapper);
    }

    @Test
    @DisplayName("el isbn llega al DTO")
    void elIsbnLlegaAlDto() {
        // Sin esto la busqueda de portadas en Open Library no tiene con que
        // trabajar: fue justamente el campo que faltaba mapear.
        Libro libro = FabricaDeLibros.disponible("Rayuela");
        libro.setIsbn("9788437604947");

        assertThat(mapper.toResponseDto(libro).isbn()).isEqualTo("9788437604947");
    }

    @Test
    @DisplayName("las categorias salen ordenadas por nombre")
    void categoriasOrdenadasPorNombre() {
        // El orden importa porque la ficha las muestra separadas por punto medio
        // y un Set sin ordenar cambiaria el texto entre dos cargas iguales.
        Libro libro = FabricaDeLibros.disponible("Un libro");
        Set<Categoria> desordenadas = new LinkedHashSet<>();
        desordenadas.add(FabricaDeLibros.categoria("Poesia"));
        desordenadas.add(FabricaDeLibros.categoria("Ensayo"));
        desordenadas.add(FabricaDeLibros.categoria("Narrativa"));
        libro.setCategorias(desordenadas);

        assertThat(mapper.toResponseDto(libro).categorias())
                .extracting(CategoriaResponseDto::nombre)
                .containsExactly("Ensayo", "Narrativa", "Poesia");
    }

    @Test
    @DisplayName("se exponen los campos que la ficha necesita y ninguno mas")
    void expoineLosCamposEsperados() {
        Libro libro = FabricaDeLibros.disponible("Dune");

        LibroResponseDto dto = mapper.toResponseDto(libro);

        assertThat(dto.id()).isEqualTo(libro.getId());
        assertThat(dto.titulo()).isEqualTo("Dune");
        assertThat(dto.autor()).isEqualTo(libro.getAutor());
        assertThat(dto.descripcion()).isEqualTo(libro.getDescripcion());
        assertThat(dto.editorial()).isEqualTo(libro.getEditorial());
        assertThat(dto.anioPublicacion()).isEqualTo(libro.getAnioPublicacion());
        assertThat(dto.portada()).isEqualTo(libro.getPortada());
        assertThat(dto.estadoLibro()).isEqualTo(libro.getEstadoLibro());
    }
}

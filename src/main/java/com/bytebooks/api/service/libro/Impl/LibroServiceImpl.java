package com.bytebooks.api.service.libro.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.libro.LibroRequestDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import com.bytebooks.api.mapper.libro.LibroMapper;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.service.categoria.CategoriaService;
import com.bytebooks.api.service.libro.LibroService;
import com.bytebooks.api.service.libro.VisibilidadDeLibros;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaService categoriaService;
    private final LibroMapper libroMapper;
    private final VisibilidadDeLibros visibilidad;

    public LibroServiceImpl(LibroRepository libroRepository,
                            CategoriaService categoriaService,
                            LibroMapper libroMapper,
                            VisibilidadDeLibros visibilidad) {
        this.libroRepository = libroRepository;
        this.categoriaService = categoriaService;
        this.libroMapper = libroMapper;
        this.visibilidad = visibilidad;
    }

    @Override
    public LibroResponseDto getLibroById(UUID id) {
        Libro libro = libroRepository.findByIdConCategorias(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + id));

        if (!visibilidad.esVisible(libro)) {
            throw new NoSuchElementException("Libro no encontrado con id: " + id);
        }

        return libroMapper.toResponseDto(libro);
    }

    /**
     * Quien puede gestionar ve todo; el resto recibe el catalogo ya filtrado por
     * la base. Antes se traia la tabla entera y se descartaban los ocultos en
     * memoria, que cuesta lo mismo tenga el catalogo diez libros o diez mil.
     */
    @Override
    public List<LibroResponseDto> getAllLibros() {
        List<Libro> libros = visibilidad.puedeGestionar()
                ? libroRepository.findAllConCategorias()
                : libroRepository.findVisiblesConCategorias(EstadoLibroEnum.OCULTO);

        return libros.stream()
                .map(libroMapper::toResponseDto)
                .toList();
    }

    @Override
    public LibroResponseDto agregarLibro(LibroRequestDto request) {
        verificarTituloLibre(request.titulo(), null);

        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        Libro libro = new Libro();
        aplicar(libro, request, categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    @Override
    public LibroResponseDto actualizarLibro(UUID id, LibroRequestDto request) {
        Libro libro = libroRepository.findByIdConCategorias(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + id));

        verificarTituloLibre(request.titulo(), id);

        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        aplicar(libro, request, categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    /**
     * El titulo es UNIQUE en la base. Sin este chequeo, cargar un libro repetido
     * llegaba hasta el insert y volvia como error de integridad, sin decir cual
     * era el problema. IllegalStateException ya esta mapeada a 409.
     *
     * Queda igual el handler de integridad: entre este chequeo y el insert hay
     * una ventana en la que otra peticion puede tomar el mismo titulo.
     */
    private void verificarTituloLibre(String titulo, UUID idQueSeEdita) {
        boolean repetido = idQueSeEdita == null
                ? libroRepository.existsByTitulo(titulo)
                : libroRepository.existsByTituloAndIdNot(titulo, idQueSeEdita);

        if (repetido) {
            throw new IllegalStateException("Ya existe un libro con el titulo: " + titulo);
        }
    }

    /**
     * Editorial, anio, portada y estado llegaban desde el formulario pero no se
     * persistian: el DTO no los declaraba y Jackson los descartaba en silencio.
     * PUT reemplaza el recurso completo, asi que un cliente que omita un campo
     * lo deja vacio; el unico consumidor los envia siempre.
     */
    private void aplicar(Libro libro, LibroRequestDto request, Set<Categoria> categorias) {
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setDescripcion(request.descripcion());
        libro.setCategorias(categorias);
        libro.setEditorial(normalizar(request.editorial()));
        libro.setAnioPublicacion(normalizar(request.anioPublicacion()));
        libro.setPortada(normalizar(request.portada()));
        libro.setEstadoLibro(request.estadoLibro() != null
                ? request.estadoLibro()
                : EstadoLibroEnum.DISPONIBLE);
    }

    /**
     * Una cadena vacia se guarda como null. Un "" en la base obliga a que cada
     * consumidor distinga despues entre "sin dato" y "dato vacio", que son lo
     * mismo para todos estos campos.
     */
    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    @Override
    public void eliminarLibro(UUID id) {
        if (!libroRepository.existsById(id)) {
            throw new NoSuchElementException("Libro no encontrado con id: " + id);
        }
        libroRepository.deleteById(id);
    }
}

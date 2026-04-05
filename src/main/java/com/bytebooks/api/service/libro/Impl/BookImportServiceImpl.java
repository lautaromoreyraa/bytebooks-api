package com.bytebooks.api.service.libro.Impl;

import com.bytebooks.api.client.GoogleBooksClient;
import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.google.GoogleBooksResponse.IndustryIdentifier;
import com.bytebooks.api.dto.google.GoogleBooksResponse.VolumeInfo;
import com.bytebooks.api.dto.google.GoogleBooksResponse.VolumeItem;
import com.bytebooks.api.dto.libro.BookImportResponseDto;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.service.libro.BookImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class BookImportServiceImpl implements BookImportService {

    private static final Logger log = LoggerFactory.getLogger(BookImportServiceImpl.class);
    private static final int TARGET_IMPORTADOS = 20;
    private static final int MAX_RESULTS_GOOGLE = 40;
    private static final int MAX_TITULO_LENGTH = 255;
    private static final int MAX_AUTOR_LENGTH = 100;
    private static final int MAX_DESCRIPCION_LENGTH = 1000;
    private static final int MAX_EDITORIAL_LENGTH = 255;
    private static final int MAX_PORTADA_LENGTH = 500;
    private static final Pattern ANIO_PATTERN = Pattern.compile("(\\d{4})");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");
    private static final Pattern COMBINING_MARKS_PATTERN = Pattern.compile("\\p{M}+");

    private final GoogleBooksClient googleBooksClient;
    private final LibroRepository libroRepository;

    public BookImportServiceImpl(GoogleBooksClient googleBooksClient, LibroRepository libroRepository) {
        this.googleBooksClient = googleBooksClient;
        this.libroRepository = libroRepository;
    }

    @Override
    public BookImportResponseDto importarDesdeGoogle(String query, Categoria categoria) {
        List<VolumeItem> items = googleBooksClient.buscar(query, MAX_RESULTS_GOOGLE);
        if (items.isEmpty()) {
            log.info("Google Books no devolvio resultados para query '{}'", query);
            return new BookImportResponseDto(query, 0, 0, 0, 0, 0);
        }

        List<String> candidatosIsbn = items.stream()
                .map(this::extraerIsbn13)
                .filter(isbn -> isbn != null && !isbn.isBlank())
                .toList();

        Set<String> isbnsExistentes = libroRepository.findIsbnsByIsbnIn(candidatosIsbn);
        Set<String> titulosExistentes = new HashSet<>();
        for (String titulo : libroRepository.findAllTitulos()) {
            String tituloNormalizado = normalizeText(titulo);
            if (!tituloNormalizado.isBlank()) {
                titulosExistentes.add(tituloNormalizado);
            }
        }

        Set<String> titulosEnBatch = new HashSet<>();
        Set<String> isbnsEnBatch = new HashSet<>();
        int guardados = 0;
        int duplicados = 0;
        int invalidos = 0;
        int errores = 0;

        for (VolumeItem item : items) {
            if (guardados >= TARGET_IMPORTADOS) {
                break;
            }

            VolumeInfo info = item.volumeInfo();
            if (info == null) {
                invalidos++;
                continue;
            }

            String titulo = sanitizeTitle(info.title());
            if (titulo == null) {
                invalidos++;
                continue;
            }

            String tituloNormalizado = normalizeText(titulo);
            String isbn = extraerIsbn13(item);

            boolean isbnDuplicado = isbn != null
                    && (!isbnsEnBatch.add(isbn) || isbnsExistentes.contains(isbn));
            boolean tituloDuplicado = !titulosEnBatch.add(tituloNormalizado)
                    || titulosExistentes.contains(tituloNormalizado);

            if (isbnDuplicado || tituloDuplicado) {
                duplicados++;
                continue;
            }

            Libro libro = new Libro();
            libro.setIsbn(isbn);
            libro.setTitulo(titulo);
            libro.setAutor(sanitizeAuthor(info.authors()));
            libro.setDescripcion(sanitizeDescription(info.description()));
            libro.setEditorial(truncate(cleanText(info.publisher()), MAX_EDITORIAL_LENGTH));
            libro.setAnioPublicacion(extraerAnio(info.publishedDate()));
            libro.setPortada(sanitizeThumbnail(info));
            libro.setCategoria(categoria);
            libro.setEstadoLibro(EstadoLibroEnum.DISPONIBLE);

            try {
                libroRepository.saveAndFlush(libro);
                guardados++;
                titulosExistentes.add(tituloNormalizado);
                if (isbn != null) {
                    isbnsExistentes.add(isbn);
                }
            } catch (DataIntegrityViolationException ex) {
                errores++;
                log.warn("No se pudo guardar libro '{}' para query '{}': {}", titulo, query, ex.getMessage());
            }
        }

        log.info(
                "Importacion Google completada para query '{}'. Encontrados: {}, guardados: {}, duplicados: {}, invalidos: {}, errores: {}",
                query,
                items.size(),
                guardados,
                duplicados,
                invalidos,
                errores
        );

        return new BookImportResponseDto(query, items.size(), guardados, duplicados, invalidos, errores);
    }

    private String extraerIsbn13(VolumeItem item) {
        if (item == null || item.volumeInfo() == null) {
            return null;
        }
        return extraerIsbn13(item.volumeInfo().industryIdentifiers());
    }

    private String extraerIsbn13(List<IndustryIdentifier> identifiers) {
        if (identifiers == null) {
            return null;
        }
        return identifiers.stream()
                .filter(i -> "ISBN_13".equals(i.type()))
                .map(IndustryIdentifier::identifier)
                .map(this::normalizeIsbn)
                .findFirst()
                .orElse(null);
    }

    private String extraerAnio(String publishedDate) {
        if (publishedDate == null || publishedDate.isBlank()) {
            return null;
        }
        var matcher = ANIO_PATTERN.matcher(publishedDate);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String sanitizeTitle(String title) {
        String sanitized = truncate(cleanText(title), MAX_TITULO_LENGTH);
        return sanitized == null || sanitized.isBlank() ? null : sanitized;
    }

    private String sanitizeAuthor(List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            return "Autor desconocido";
        }
        String joined = cleanText(String.join(", ", authors));
        if (joined == null || joined.isBlank()) {
            return "Autor desconocido";
        }
        return truncate(joined, MAX_AUTOR_LENGTH);
    }

    private String sanitizeDescription(String description) {
        String sanitized = cleanText(description);
        if (sanitized == null || sanitized.isBlank()) {
            return "Sin descripcion disponible.";
        }
        return truncate(sanitized, MAX_DESCRIPCION_LENGTH);
    }

    private String sanitizeThumbnail(VolumeInfo info) {
        if (info.imageLinks() == null) {
            return null;
        }

        String imageUrl = firstNonBlank(
                info.imageLinks().extraLarge(),
                info.imageLinks().large(),
                info.imageLinks().medium(),
                info.imageLinks().small(),
                info.imageLinks().thumbnail(),
                info.imageLinks().smallThumbnail()
        );

        String thumbnail = cleanText(imageUrl);
        if (thumbnail == null || thumbnail.isBlank()) {
            return null;
        }
        String httpsThumbnail = thumbnail.startsWith("http://")
                ? "https://" + thumbnail.substring("http://".length())
                : thumbnail;
        String enhancedThumbnail = httpsThumbnail
                .replace("zoom=1", "zoom=3")
                .replace("&edge=curl", "");
        return truncate(enhancedThumbnail, MAX_PORTADA_LENGTH);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String normalizeIsbn(String isbn) {
        if (isbn == null) {
            return null;
        }
        String normalized = isbn.replaceAll("[^0-9Xx]", "").trim();
        return normalized.isBlank() ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = MULTIPLE_SPACES_PATTERN.matcher(value).replaceAll(" ").trim();
        return collapsed.isBlank() ? null : collapsed;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String normalizeText(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null) {
            return "";
        }
        String normalized = Normalizer.normalize(cleaned, Normalizer.Form.NFD);
        String withoutAccents = COMBINING_MARKS_PATTERN.matcher(normalized).replaceAll("");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }
}

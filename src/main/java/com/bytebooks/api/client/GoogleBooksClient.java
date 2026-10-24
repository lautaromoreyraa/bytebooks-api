package com.bytebooks.api.client;

import com.bytebooks.api.dto.google.GoogleBooksResponse;
import com.bytebooks.api.exception.GoogleBooksNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;

@Component
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);
    private static final String BASE_URL = "https://www.googleapis.com/books/v1";

    /**
     * Google Books rechaza de a ratos pedidos válidos con un 503
     * («backendFailed») y deja pasar el siguiente. Tres intentos alcanzan para
     * que esa falla no le llegue al usuario sin demorar mucho una búsqueda
     * cuando Google está caído de verdad.
     */
    private static final int INTENTOS = 3;

    @Value("${google.books.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final Duration pausaEntreIntentos;

    @Autowired
    public GoogleBooksClient(RestClient.Builder builder) {
        this(builder, Duration.ofMillis(400));
    }

    /** Los tests pasan una pausa en cero para no esperar entre intentos. */
    GoogleBooksClient(RestClient.Builder builder, Duration pausaEntreIntentos) {
        this.restClient = builder.baseUrl(BASE_URL).build();
        this.pausaEntreIntentos = pausaEntreIntentos;
    }

    /**
     * Busca volúmenes en Google Books. Si Google no contesta —un 5xx o un corte
     * de red— reintenta con una pausa que crece; si después de los reintentos
     * sigue sin contestar, lanza {@link GoogleBooksNoDisponibleException} para
     * que la pantalla diga que Google no responde y no que no hubo resultados.
     */
    public List<GoogleBooksResponse.VolumeItem> buscar(String query, int maxResults) {
        for (int intento = 1; ; intento++) {
            try {
                return consultar(query, maxResults);
            } catch (HttpServerErrorException | ResourceAccessException e) {
                log.warn("Google Books no respondió (intento {} de {}): {}", intento, INTENTOS, e.getMessage());
                if (intento == INTENTOS) {
                    throw new GoogleBooksNoDisponibleException();
                }
                esperar(intento);
            } catch (HttpClientErrorException e) {
                // Cuota agotada o clave rechazada: reintentar no cambia nada.
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS ||
                        e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    log.error("Google Books respondió: {}", e.getResponseBodyAsString());
                    throw new GoogleBooksNoDisponibleException();
                }
                log.error("Error HTTP al consultar Google Books API: {} {}", e.getStatusCode(), e.getMessage());
                return List.of();
            } catch (Exception e) {
                log.error("Error al consultar Google Books API: {}", e.getMessage());
                return List.of();
            }
        }
    }

    private List<GoogleBooksResponse.VolumeItem> consultar(String query, int maxResults) {
        GoogleBooksResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", query)
                        .queryParam("maxResults", maxResults)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(GoogleBooksResponse.class);

        return response != null && response.items() != null ? response.items() : List.of();
    }

    private void esperar(int intento) {
        try {
            Thread.sleep(pausaEntreIntentos.multipliedBy(intento));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GoogleBooksNoDisponibleException();
        }
    }
}

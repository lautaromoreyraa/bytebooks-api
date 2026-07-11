package com.bytebooks.api.client;

import com.bytebooks.api.dto.google.GoogleBooksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Component
public class GoogleBooksClient {

    @Value("${google.books.api.key}")
    private String apiKey;

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);
    private static final String BASE_URL = "https://www.googleapis.com/books/v1";

    private final RestClient restClient;

    public GoogleBooksClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    public List<GoogleBooksResponse.VolumeItem> buscar(String query, int maxResults) {
        try {
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
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS ||
                    e.getStatusCode() == HttpStatus.FORBIDDEN) {

                log.error("Google Books respondió: {}", e.getResponseBodyAsString());

                throw new IllegalStateException(
                        "No fue posible consultar Google Books."
                );
            }
            log.error("Error HTTP al consultar Google Books API: {} {}", e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Error al consultar Google Books API: {}", e.getMessage());
            return List.of();
        }
    }
}

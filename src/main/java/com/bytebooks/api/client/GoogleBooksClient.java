package com.bytebooks.api.client;

import com.bytebooks.api.dto.google.GoogleBooksResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);
    private static final String BASE_URL = "https://www.googleapis.com/books/v1";

    private final RestClient restClient;

    public GoogleBooksClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    public List<GoogleBooksResponse.VolumeItem> buscar(String query, int maxResults) {
        try {
            GoogleBooksResponse response = restClient.get()
                    .uri("/volumes?q={q}&maxResults={max}", query, maxResults)
                    .retrieve()
                    .body(GoogleBooksResponse.class);

            return response != null && response.items() != null ? response.items() : List.of();
        } catch (Exception e) {
            log.error("Error al consultar Google Books API: {}", e.getMessage());
            return List.of();
        }
    }
}

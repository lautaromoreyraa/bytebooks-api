package com.bytebooks.api.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBooksResponse(List<VolumeItem> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VolumeItem(VolumeInfo volumeInfo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VolumeInfo(
            String title,
            List<String> authors,
            String description,
            String publisher,
            String publishedDate,
            ImageLinks imageLinks,
            List<IndustryIdentifier> industryIdentifiers
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageLinks(
            String smallThumbnail,
            String thumbnail,
            String small,
            String medium,
            String large,
            String extraLarge
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IndustryIdentifier(String type, String identifier) {}
}

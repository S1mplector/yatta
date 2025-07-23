package com.animetui.application.dto;

import java.util.List;

/**
 * Data Transfer Object for anime information.
 * Lightweight representation for presentation layer.
 */
public record AnimeDto(
    String id,
    String title,
    String synopsis,
    String imageUrl,
    int episodeCount,
    String status,
    String airingDate,
    List<String> genres
) {
    public AnimeDto {
        // Defensive copy of mutable list
        genres = genres == null ? List.of() : List.copyOf(genres);
    }
}

package com.animetui.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain model representing an anime series.
 * Immutable record following domain-driven design principles.
 */
public record Anime(
    String id,
    String title,
    String synopsis,
    String imageUrl,
    int episodeCount,
    String status,
    LocalDate airingDate,
    List<String> genres
) {
    public Anime {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Anime ID cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Anime title cannot be null or blank");
        }
        if (episodeCount < 0) {
            throw new IllegalArgumentException("Episode count cannot be negative");
        }
        
        // Defensive copy of mutable list
        genres = genres == null ? List.of() : List.copyOf(genres);
    }
    
    /**
     * Check if the anime is currently airing.
     */
    public boolean isAiring() {
        return "Airing".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the anime has finished airing.
     */
    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }
}

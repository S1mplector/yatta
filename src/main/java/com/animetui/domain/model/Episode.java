package com.animetui.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model representing an anime episode.
 * Immutable record following domain-driven design principles.
 */
public record Episode(
    String id,
    String animeId,
    int number,
    String title,
    String description,
    int durationMinutes,
    LocalDateTime airDate,
    String thumbnailUrl
) {
    public Episode {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Episode ID cannot be null or blank");
        }
        if (animeId == null || animeId.isBlank()) {
            throw new IllegalArgumentException("Anime ID cannot be null or blank");
        }
        if (number <= 0) {
            throw new IllegalArgumentException("Episode number must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Episode title cannot be null or blank");
        }
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
    }
    
    /**
     * Get a formatted display string for the episode.
     */
    public String getDisplayTitle() {
        return String.format("Episode %d: %s", number, title);
    }
    
    /**
     * Check if the episode has aired.
     */
    public boolean hasAired() {
        return airDate != null && airDate.isBefore(LocalDateTime.now());
    }
}

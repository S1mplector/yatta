package com.animetui.application.dto;

/**
 * Data Transfer Object for episode information.
 * Lightweight representation for presentation layer.
 */
public record EpisodeDto(
    String id,
    String animeId,
    int number,
    String title,
    String description,
    int durationMinutes,
    String airDate,
    String thumbnailUrl
) {
}

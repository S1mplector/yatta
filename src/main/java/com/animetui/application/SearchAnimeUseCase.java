package com.animetui.application;

import com.animetui.application.dto.AnimeDto;
import com.animetui.domain.model.Anime;
import com.animetui.domain.port.AnimeRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Use case for searching anime by title or keywords.
 * Orchestrates domain ports to fulfill user search requests.
 */
public class SearchAnimeUseCase {
    
    private final AnimeRepository animeRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public SearchAnimeUseCase(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }
    
    /**
     * Search anime with default limit.
     */
    public List<AnimeDto> execute(String query) {
        return execute(query, 20); // Default limit
    }
    
    /**
     * Search anime with specified limit.
     */
    public List<AnimeDto> execute(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be null or blank");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        
        List<Anime> anime = animeRepository.search(query.trim(), limit);
        return anime.stream()
                .map(this::toDto)
                .toList();
    }
    
    private AnimeDto toDto(Anime anime) {
        String airingDate = anime.airingDate() != null ? 
            anime.airingDate().format(DATE_FORMATTER) : null;
            
        return new AnimeDto(
            anime.id(),
            anime.title(),
            anime.synopsis(),
            anime.imageUrl(),
            anime.episodeCount(),
            anime.status(),
            airingDate,
            anime.genres()
        );
    }
}

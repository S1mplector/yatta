package com.animetui.application;

import com.animetui.application.dto.AnimeDto;
import com.animetui.domain.model.Anime;
import com.animetui.domain.port.AnimeRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Use case for fetching anime catalog data.
 * Orchestrates domain ports to fulfill user requests for anime listings.
 */
public class FetchCatalogUseCase {
    
    private final AnimeRepository animeRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public FetchCatalogUseCase(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }
    
    /**
     * Fetch popular anime with default limit.
     */
    public List<AnimeDto> execute() {
        return execute(20); // Default limit
    }
    
    /**
     * Fetch popular anime with specified limit.
     */
    public List<AnimeDto> execute(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        
        List<Anime> anime = animeRepository.listPopular(limit);
        return anime.stream()
                .map(this::toDto)
                .toList();
    }
    
    /**
     * Fetch currently airing anime.
     */
    public List<AnimeDto> executeCurrentSeason(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        
        List<Anime> anime = animeRepository.getCurrentSeason(limit);
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

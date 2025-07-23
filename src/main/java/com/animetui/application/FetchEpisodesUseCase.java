package com.animetui.application;

import com.animetui.application.dto.EpisodeDto;
import com.animetui.domain.model.Anime;
import com.animetui.domain.model.Episode;
import com.animetui.domain.port.AnimeRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Use case for fetching episode data for a specific anime.
 * Orchestrates domain ports to fulfill user requests for episode listings.
 */
public class FetchEpisodesUseCase {
    
    private final AnimeRepository animeRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public FetchEpisodesUseCase(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }
    
    /**
     * Fetch episodes for the given anime.
     */
    public List<EpisodeDto> execute(Anime anime) {
        if (anime == null) {
            throw new IllegalArgumentException("Anime cannot be null");
        }
        
        List<Episode> episodes = animeRepository.episodesOf(anime);
        return episodes.stream()
                .map(this::toDto)
                .toList();
    }
    
    /**
     * Fetch episodes by anime ID.
     */
    public List<EpisodeDto> execute(String animeId) {
        if (animeId == null || animeId.isBlank()) {
            throw new IllegalArgumentException("Anime ID cannot be null or blank");
        }
        
        List<Episode> episodes = animeRepository.episodesById(animeId);
        return episodes.stream()
                .map(this::toDto)
                .toList();
    }
    
    private EpisodeDto toDto(Episode episode) {
        String airDate = episode.airDate() != null ? 
            episode.airDate().format(DATE_TIME_FORMATTER) : null;
            
        return new EpisodeDto(
            episode.id(),
            episode.animeId(),
            episode.number(),
            episode.title(),
            episode.description(),
            episode.durationMinutes(),
            airDate,
            episode.thumbnailUrl()
        );
    }
}

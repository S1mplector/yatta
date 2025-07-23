package com.animetui.domain.port;

import com.animetui.domain.model.Anime;
import com.animetui.domain.model.Episode;

import java.util.List;
import java.util.Optional;

/**
 * Port for accessing anime data from external sources.
 * This interface defines the contract for anime repository implementations.
 */
public interface AnimeRepository {
    
    /**
     * Retrieve a list of popular anime with a specified limit.
     * 
     * @param limit maximum number of anime to return
     * @return list of popular anime
     */
    List<Anime> listPopular(int limit);
    
    /**
     * Search for anime by title or keywords.
     * 
     * @param query search query string
     * @param limit maximum number of results to return
     * @return list of matching anime
     */
    List<Anime> search(String query, int limit);
    
    /**
     * Get detailed information about a specific anime by ID.
     * 
     * @param animeId unique identifier for the anime
     * @return anime details if found
     */
    Optional<Anime> findById(String animeId);
    
    /**
     * Retrieve all episodes for a specific anime.
     * 
     * @param anime the anime to get episodes for
     * @return list of episodes in order
     */
    List<Episode> episodesOf(Anime anime);
    
    /**
     * Get episodes for a specific anime by ID.
     * 
     * @param animeId unique identifier for the anime
     * @return list of episodes in order
     */
    List<Episode> episodesById(String animeId);
    
    /**
     * Get currently airing anime for the current season.
     * 
     * @param limit maximum number of anime to return
     * @return list of currently airing anime
     */
    List<Anime> getCurrentSeason(int limit);
}

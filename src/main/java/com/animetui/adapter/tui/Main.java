package com.animetui.adapter.tui;

import com.animetui.application.FetchCatalogUseCase;
import com.animetui.application.FetchEpisodesUseCase;
import com.animetui.application.PlayEpisodeUseCase;
import com.animetui.application.SearchAnimeUseCase;
import com.animetui.domain.port.AnimeRepository;
import com.animetui.domain.port.ConfigPort;
import com.animetui.domain.port.LinkResolver;
import com.animetui.domain.port.MediaPlayerPort;
import com.animetui.infrastructure.config.AppConfig;
import com.animetui.infrastructure.player.MpvPlayerAdapter;
import com.animetui.infrastructure.scraper.JikanAnimeScraper;
import com.animetui.infrastructure.scraper.LinkResolverFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Main entry point for the Anime-TUI application.
 * Bootstraps the dependency graph and starts the TUI.
 */
public class Main {
    
    public static void main(String[] args) {
        try {
            // Load configuration
            ConfigPort config = AppConfig.load();
            
            // Initialize infrastructure adapters
            AnimeRepository animeRepository = createAnimeRepository(config);
            LinkResolver linkResolver = createLinkResolver(config);
            MediaPlayerPort mediaPlayer = createMediaPlayer(config);
            
            // Initialize use cases
            FetchCatalogUseCase fetchCatalog = new FetchCatalogUseCase(animeRepository);
            FetchEpisodesUseCase fetchEpisodes = new FetchEpisodesUseCase(animeRepository);
            SearchAnimeUseCase searchAnime = new SearchAnimeUseCase(animeRepository);
            PlayEpisodeUseCase playEpisode = new PlayEpisodeUseCase(linkResolver, mediaPlayer);
            
            // Initialize and run TUI
            AnimeTui tui = new AnimeTui(fetchCatalog, fetchEpisodes, playEpisode, searchAnime);
            tui.run();
            
        } catch (Exception e) {
            System.err.println("Failed to start Anime-TUI: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static AnimeRepository createAnimeRepository(ConfigPort config) {
        String baseUrl = config.getString("scraper.baseUrl", "https://api.jikan.moe/v4");
        return new JikanAnimeScraper(baseUrl);
    }
    
    private static LinkResolver createLinkResolver(ConfigPort config) {
        return LinkResolverFactory.create(config);
    }
    
    private static MediaPlayerPort createMediaPlayer(ConfigPort config) {
        String playerCommand = config.getString("player.command", "mpv");
        String argsString = config.getString("player.args", "--no-terminal");
        
        List<String> args = Arrays.stream(argsString.split("\\s+"))
                .filter(arg -> !arg.trim().isEmpty())
                .toList();
        
        MediaPlayerPort player = new MpvPlayerAdapter(playerCommand, args);
        
        // Check if player is available
        if (!player.isAvailable()) {
            System.err.println("Warning: Media player '" + playerCommand + "' is not available on this system.");
            System.err.println("Please install " + playerCommand + " or configure a different player in application.properties");
        }
        
        return player;
    }
}

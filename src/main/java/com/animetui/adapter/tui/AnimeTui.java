package com.animetui.adapter.tui;

import com.animetui.application.FetchCatalogUseCase;
import com.animetui.application.FetchEpisodesUseCase;
import com.animetui.application.PlayEpisodeUseCase;
import com.animetui.application.SearchAnimeUseCase;
import com.animetui.application.dto.AnimeDto;
import com.animetui.application.dto.EpisodeDto;
import com.animetui.domain.model.Anime;
import com.animetui.domain.model.Episode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Terminal User Interface for the Anime TUI application.
 * Main driver that coordinates user interactions with application use-cases.
 */
public class AnimeTui {
    
    private final FetchCatalogUseCase fetchCatalog;
    private final FetchEpisodesUseCase fetchEpisodes;
    private final PlayEpisodeUseCase playEpisode;
    private final SearchAnimeUseCase searchAnime;
    private final Scanner scanner;
    
    public AnimeTui(FetchCatalogUseCase fetchCatalog,
                    FetchEpisodesUseCase fetchEpisodes,
                    PlayEpisodeUseCase playEpisode,
                    SearchAnimeUseCase searchAnime) {
        this.fetchCatalog = fetchCatalog;
        this.fetchEpisodes = fetchEpisodes;
        this.playEpisode = playEpisode;
        this.searchAnime = searchAnime;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Main application loop.
     */
    public void run() {
        ViewHelpers.printHeader("Welcome to Anime-TUI");
        ViewHelpers.printInfo("Your terminal-based anime streaming application");
        
        while (true) {
            try {
                int choice = showMainMenu();
                
                switch (choice) {
                    case 0 -> browsePopularAnime();
                    case 1 -> browseCurrentSeason();
                    case 2 -> searchForAnime();
                    case 3 -> {
                        ViewHelpers.printInfo("Thank you for using Anime-TUI!");
                        return;
                    }
                    default -> ViewHelpers.printError("Invalid choice");
                }
            } catch (Exception e) {
                ViewHelpers.printError("An error occurred: " + e.getMessage());
                ViewHelpers.waitForEnter(scanner);
            }
        }
    }
    
    private int showMainMenu() {
        List<String> options = List.of(
            "Browse Popular Anime",
            "Browse Current Season",
            "Search Anime",
            "Exit"
        );
        
        return ViewHelpers.showMenu("Main Menu", options, scanner);
    }
    
    private void browsePopularAnime() {
        ViewHelpers.showLoading("Fetching popular anime");
        
        try {
            List<AnimeDto> animeList = fetchCatalog.execute(15);
            ViewHelpers.clearLoading();
            
            if (animeList.isEmpty()) {
                ViewHelpers.printWarning("No anime found");
                return;
            }
            
            int selectedIndex = ViewHelpers.pickFromList(
                "Select an anime to view episodes:",
                animeList,
                this::formatAnimeDisplay,
                scanner
            );
            
            if (selectedIndex >= 0) {
                showAnimeDetails(animeList.get(selectedIndex));
            }
            
        } catch (Exception e) {
            ViewHelpers.clearLoading();
            ViewHelpers.printError("Failed to fetch popular anime: " + e.getMessage());
        }
        
        ViewHelpers.waitForEnter(scanner);
    }
    
    private void browseCurrentSeason() {
        ViewHelpers.showLoading("Fetching current season anime");
        
        try {
            List<AnimeDto> animeList = fetchCatalog.executeCurrentSeason(15);
            ViewHelpers.clearLoading();
            
            if (animeList.isEmpty()) {
                ViewHelpers.printWarning("No anime found for current season");
                return;
            }
            
            int selectedIndex = ViewHelpers.pickFromList(
                "Select an anime to view episodes:",
                animeList,
                this::formatAnimeDisplay,
                scanner
            );
            
            if (selectedIndex >= 0) {
                showAnimeDetails(animeList.get(selectedIndex));
            }
            
        } catch (Exception e) {
            ViewHelpers.clearLoading();
            ViewHelpers.printError("Failed to fetch current season anime: " + e.getMessage());
        }
        
        ViewHelpers.waitForEnter(scanner);
    }
    
    private void searchForAnime() {
        String query = ViewHelpers.getInput("Enter search query:", scanner);
        
        if (query.isEmpty()) {
            ViewHelpers.printWarning("Search query cannot be empty");
            return;
        }
        
        ViewHelpers.showLoading("Searching for anime");
        
        try {
            List<AnimeDto> animeList = searchAnime.execute(query, 15);
            ViewHelpers.clearLoading();
            
            if (animeList.isEmpty()) {
                ViewHelpers.printWarning("No anime found matching your search");
                return;
            }
            
            int selectedIndex = ViewHelpers.pickFromList(
                "Search Results - Select an anime:",
                animeList,
                this::formatAnimeDisplay,
                scanner
            );
            
            if (selectedIndex >= 0) {
                showAnimeDetails(animeList.get(selectedIndex));
            }
            
        } catch (Exception e) {
            ViewHelpers.clearLoading();
            ViewHelpers.printError("Search failed: " + e.getMessage());
        }
        
        ViewHelpers.waitForEnter(scanner);
    }
    
    private void showAnimeDetails(AnimeDto animeDto) {
        ViewHelpers.printHeader(animeDto.title());
        
        System.out.println("Status: " + animeDto.status());
        System.out.println("Episodes: " + (animeDto.episodeCount() > 0 ? animeDto.episodeCount() : "Unknown"));
        if (animeDto.airingDate() != null) {
            System.out.println("Aired: " + animeDto.airingDate());
        }
        if (!animeDto.genres().isEmpty()) {
            System.out.println("Genres: " + String.join(", ", animeDto.genres()));
        }
        
        if (animeDto.synopsis() != null && !animeDto.synopsis().isEmpty()) {
            System.out.println("\nSynopsis:");
            System.out.println(wrapText(animeDto.synopsis(), 80));
        }
        
        System.out.println();
        String choice = ViewHelpers.getInput("View episodes? (y/n):", scanner);
        
        if ("y".equalsIgnoreCase(choice) || "yes".equalsIgnoreCase(choice)) {
            showEpisodes(animeDto);
        }
    }
    
    private void showEpisodes(AnimeDto animeDto) {
        ViewHelpers.showLoading("Fetching episodes");
        
        try {
            List<EpisodeDto> episodes = fetchEpisodes.execute(animeDto.id());
            ViewHelpers.clearLoading();
            
            if (episodes.isEmpty()) {
                ViewHelpers.printWarning("No episodes found for this anime");
                return;
            }
            
            int selectedIndex = ViewHelpers.pickFromList(
                "Select an episode to play:",
                episodes,
                this::formatEpisodeDisplay,
                scanner
            );
            
            if (selectedIndex >= 0) {
                playSelectedEpisode(episodes.get(selectedIndex), animeDto.title());
            }
            
        } catch (Exception e) {
            ViewHelpers.clearLoading();
            ViewHelpers.printError("Failed to fetch episodes: " + e.getMessage());
        }
    }
    
    private void playSelectedEpisode(EpisodeDto episodeDto, String animeTitle) {
        ViewHelpers.printInfo("Starting playback for: " + formatEpisodeDisplay(episodeDto));
        ViewHelpers.showLoading("Resolving stream links");
        
        try {
            Episode episode = convertToEpisode(episodeDto, animeTitle);
            playEpisode.execute(episode);
            ViewHelpers.clearLoading();
            ViewHelpers.printSuccess("Episode playback started!");
            
        } catch (Exception e) {
            ViewHelpers.clearLoading();
            ViewHelpers.printError("Failed to play episode: " + e.getMessage());
        }
    }
    
    private String formatAnimeDisplay(AnimeDto anime) {
        StringBuilder display = new StringBuilder();
        display.append(anime.title());
        
        if (anime.episodeCount() > 0) {
            display.append(" (").append(anime.episodeCount()).append(" eps)");
        }
        
        display.append(" - ").append(anime.status());
        
        return display.toString();
    }
    
    private String formatEpisodeDisplay(EpisodeDto episode) {
        return String.format("Episode %d: %s", episode.number(), episode.title());
    }
    
    private String wrapText(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split("\\s+");
        int currentLineLength = 0;
        
        for (String word : words) {
            if (currentLineLength + word.length() + 1 > width) {
                wrapped.append("\n");
                currentLineLength = 0;
            }
            
            if (currentLineLength > 0) {
                wrapped.append(" ");
                currentLineLength++;
            }
            
            wrapped.append(word);
            currentLineLength += word.length();
        }
        
        return wrapped.toString();
    }
    
    private Episode convertToEpisode(EpisodeDto dto, String animeTitle) {
        LocalDateTime airDate = null;
        if (dto.airDate() != null && !dto.airDate().isEmpty()) {
            try {
                airDate = LocalDateTime.parse(dto.airDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        return new Episode(
            dto.id(),
            dto.animeId(),
            animeTitle, // Now we properly pass the anime title
            dto.number(),
            dto.title(),
            dto.description(),
            dto.durationMinutes(),
            airDate,
            dto.thumbnailUrl()
        );
    }
}

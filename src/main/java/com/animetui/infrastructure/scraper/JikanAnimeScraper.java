package com.animetui.infrastructure.scraper;

import com.animetui.domain.model.Anime;
import com.animetui.domain.model.Episode;
import com.animetui.domain.port.AnimeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Infrastructure implementation of AnimeRepository using Jikan API.
 * Fetches anime data from MyAnimeList via the Jikan REST API.
 */
public class JikanAnimeScraper implements AnimeRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(JikanAnimeScraper.class);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public JikanAnimeScraper(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.jikan.moe/v4";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public JikanAnimeScraper() {
        this("https://api.jikan.moe/v4");
    }
    
    @Override
    public List<Anime> listPopular(int limit) {
        try {
            String url = baseUrl + "/top/anime?limit=" + limit;
            JsonNode response = makeRequest(url);
            return parseAnimeList(response.get("data"));
        } catch (Exception e) {
            logger.error("Failed to fetch popular anime", e);
            throw new RuntimeException("Failed to fetch popular anime", e);
        }
    }
    
    @Override
    public List<Anime> search(String query, int limit) {
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String url = baseUrl + "/anime?q=" + encodedQuery + "&limit=" + limit;
            JsonNode response = makeRequest(url);
            return parseAnimeList(response.get("data"));
        } catch (Exception e) {
            logger.error("Failed to search anime with query: {}", query, e);
            throw new RuntimeException("Failed to search anime", e);
        }
    }
    
    @Override
    public Optional<Anime> findById(String animeId) {
        try {
            String url = baseUrl + "/anime/" + animeId;
            JsonNode response = makeRequest(url);
            return Optional.of(parseAnime(response.get("data")));
        } catch (Exception e) {
            logger.error("Failed to find anime by ID: {}", animeId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Episode> episodesOf(Anime anime) {
        return episodesById(anime.id());
    }
    
    @Override
    public List<Episode> episodesById(String animeId) {
        try {
            String url = baseUrl + "/anime/" + animeId + "/episodes";
            JsonNode response = makeRequest(url);
            return parseEpisodeList(response.get("data"), animeId);
        } catch (Exception e) {
            logger.error("Failed to fetch episodes for anime ID: {}", animeId, e);
            throw new RuntimeException("Failed to fetch episodes", e);
        }
    }
    
    @Override
    public List<Anime> getCurrentSeason(int limit) {
        try {
            String url = baseUrl + "/seasons/now?limit=" + limit;
            JsonNode response = makeRequest(url);
            return parseAnimeList(response.get("data"));
        } catch (Exception e) {
            logger.error("Failed to fetch current season anime", e);
            throw new RuntimeException("Failed to fetch current season anime", e);
        }
    }
    
    private JsonNode makeRequest(String url) throws IOException, InterruptedException {
        logger.debug("Making request to: {}", url);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("User-Agent", "Anime-TUI/0.1.0")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
        
        return objectMapper.readTree(response.body());
    }
    
    private List<Anime> parseAnimeList(JsonNode dataNode) {
        List<Anime> animeList = new ArrayList<>();
        
        if (dataNode != null && dataNode.isArray()) {
            for (JsonNode animeNode : dataNode) {
                try {
                    animeList.add(parseAnime(animeNode));
                } catch (Exception e) {
                    logger.warn("Failed to parse anime entry", e);
                }
            }
        }
        
        return animeList;
    }
    
    private Anime parseAnime(JsonNode animeNode) {
        String id = animeNode.get("mal_id").asText();
        String title = animeNode.get("title").asText();
        String synopsis = animeNode.has("synopsis") ? animeNode.get("synopsis").asText() : "";
        
        String imageUrl = null;
        if (animeNode.has("images") && animeNode.get("images").has("jpg")) {
            imageUrl = animeNode.get("images").get("jpg").get("image_url").asText();
        }
        
        int episodeCount = animeNode.has("episodes") && !animeNode.get("episodes").isNull() ? 
                animeNode.get("episodes").asInt() : 0;
        
        String status = animeNode.has("status") ? animeNode.get("status").asText() : "Unknown";
        
        LocalDate airingDate = null;
        if (animeNode.has("aired") && animeNode.get("aired").has("from") && 
            !animeNode.get("aired").get("from").isNull()) {
            String dateStr = animeNode.get("aired").get("from").asText();
            try {
                airingDate = LocalDate.parse(dateStr.substring(0, 10));
            } catch (Exception e) {
                logger.debug("Failed to parse airing date: {}", dateStr);
            }
        }
        
        List<String> genres = new ArrayList<>();
        if (animeNode.has("genres") && animeNode.get("genres").isArray()) {
            for (JsonNode genreNode : animeNode.get("genres")) {
                genres.add(genreNode.get("name").asText());
            }
        }
        
        return new Anime(id, title, synopsis, imageUrl, episodeCount, status, airingDate, genres);
    }
    
    private List<Episode> parseEpisodeList(JsonNode dataNode, String animeId) {
        List<Episode> episodes = new ArrayList<>();
        
        if (dataNode != null && dataNode.isArray()) {
            for (JsonNode episodeNode : dataNode) {
                try {
                    episodes.add(parseEpisode(episodeNode, animeId));
                } catch (Exception e) {
                    logger.warn("Failed to parse episode entry", e);
                }
            }
        }
        
        return episodes;
    }
    
    private Episode parseEpisode(JsonNode episodeNode, String animeId) {
        String id = animeId + "_ep_" + episodeNode.get("mal_id").asText();
        int number = episodeNode.get("mal_id").asInt();
        String title = episodeNode.has("title") ? episodeNode.get("title").asText() : "Episode " + number;
        String description = episodeNode.has("synopsis") ? episodeNode.get("synopsis").asText() : "";
        
        int duration = 24; // Default anime episode duration
        
        LocalDateTime airDate = null;
        if (episodeNode.has("aired") && !episodeNode.get("aired").isNull()) {
            String dateStr = episodeNode.get("aired").asText();
            try {
                airDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                logger.debug("Failed to parse episode air date: {}", dateStr);
            }
        }
        
        String thumbnailUrl = null;
        // Jikan API doesn't provide episode thumbnails in the basic endpoint
        
        return new Episode(id, animeId, number, title, description, duration, airDate, thumbnailUrl);
    }
}

package com.animetui.infrastructure.scraper;

import com.animetui.domain.model.Episode;
import com.animetui.domain.model.SourceSite;
import com.animetui.domain.model.StreamLink;
import com.animetui.domain.port.LinkResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A realistic mock LinkResolver that simulates finding actual anime streaming links.
 * This provides more realistic URLs and behavior for development and testing.
 */
public class RealisticMockLinkResolver implements LinkResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(RealisticMockLinkResolver.class);
    
    // Sample realistic streaming URLs (these are just examples - not real streams)
    private static final List<String> SAMPLE_STREAM_URLS = Arrays.asList(
        "https://example-cdn1.com/anime/tokyo-ghoul/ep1/720p.m3u8",
        "https://example-cdn2.com/streams/frieren/episode-1/1080p.mp4",
        "https://example-cdn3.com/video/anime/ep1/480p.m3u8",
        "https://example-streaming.com/hls/anime-episode/720p/playlist.m3u8"
    );
    
    private static final Map<String, String> ANIME_MAPPINGS = Map.of(
        "Tokyo Ghoul", "tokyo-ghoul",
        "Sousou no Frieren", "frieren",
        "Fullmetal Alchemist", "fma-brotherhood",
        "Attack on Titan", "attack-on-titan",
        "One Piece", "one-piece"
    );
    
    @Override
    public List<StreamLink> resolve(Episode episode) {
        logger.info("Resolving realistic mock links for episode: {}", episode.getDisplayTitle());
        
        // Simulate some processing time
        try {
            Thread.sleep(500 + ThreadLocalRandom.current().nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String animeSlug = getAnimeSlug(episode.animeTitle());
        int episodeNumber = episode.number();
        
        logger.info("Found anime slug: {} for episode {}", animeSlug, episodeNumber);
        
        return Arrays.asList(
            new StreamLink(
                generateRealisticUrl(animeSlug, episodeNumber, "1080p"),
                "1080p",
                "mp4",
                SourceSite.MOCK_REALISTIC.getName(),
                true
            ),
            new StreamLink(
                generateRealisticUrl(animeSlug, episodeNumber, "720p"),
                "720p", 
                "m3u8",
                SourceSite.MOCK_REALISTIC.getName(),
                false
            ),
            new StreamLink(
                generateRealisticUrl(animeSlug, episodeNumber, "480p"),
                "480p",
                "mp4", 
                SourceSite.MOCK_REALISTIC.getName(),
                true
            )
        );
    }
    
    @Override
    public StreamLink resolveBest(Episode episode) {
        List<StreamLink> links = resolve(episode);
        return links.isEmpty() ? null : links.get(0); // Return highest quality (1080p)
    }
    
    @Override
    public boolean canResolve(Episode episode) {
        // This mock resolver can "resolve" any episode
        return true;
    }
    
    private String getAnimeSlug(String animeTitle) {
        if (animeTitle == null) {
            return "unknown-anime";
        }
        
        // Check for exact matches first
        String slug = ANIME_MAPPINGS.get(animeTitle);
        if (slug != null) {
            return slug;
        }
        
        // Generate slug from title
        return animeTitle.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }
    
    private String generateRealisticUrl(String animeSlug, int episodeNumber, String quality) {
        String baseUrl = SAMPLE_STREAM_URLS.get(ThreadLocalRandom.current().nextInt(SAMPLE_STREAM_URLS.size()));
        
        // Replace parts of the URL to make it specific to this anime/episode
        return baseUrl.replaceAll("tokyo-ghoul|frieren|anime", animeSlug)
                     .replaceAll("ep1|episode-1", "ep" + episodeNumber)
                     .replaceAll("720p|1080p|480p", quality);
    }
}

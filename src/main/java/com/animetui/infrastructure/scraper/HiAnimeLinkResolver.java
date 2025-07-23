package com.animetui.infrastructure.scraper;

import com.animetui.domain.model.Episode;
import com.animetui.domain.model.StreamLink;
import com.animetui.domain.port.LinkResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LinkResolver implementation for HiAnime.to streaming site.
 * Extracts direct stream links from episode pages by parsing embedded JSON data.
 */
public class HiAnimeLinkResolver implements LinkResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(HiAnimeLinkResolver.class);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private boolean sessionInitialized = false;
    
    // Pattern to match JSON data embedded in script tags
    private static final Pattern JSON_PATTERN = Pattern.compile(
        "(?:window\\.__NUXT__|__NUXT__\\s*=)\\s*(.+?)(?:;\\s*</script>|$)", 
        Pattern.DOTALL
    );
    
    // Pattern to extract stream URLs from various formats
    private static final Pattern STREAM_URL_PATTERN = Pattern.compile(
        "(?:\"url\"|'url')\\s*:\\s*(?:\"|')([^\"']+\\.(?:mp4|m3u8|ts))(?:\"|')",
        Pattern.CASE_INSENSITIVE
    );
    
    public HiAnimeLinkResolver(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://hianime.to";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1)  // Some sites prefer HTTP/1.1
                .cookieHandler(new java.net.CookieManager())
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public HiAnimeLinkResolver() {
        this("https://hianime.to");
    }
    
    @Override
    public List<StreamLink> resolve(Episode episode) {
        logger.info("Resolving HiAnime links for episode: {}", episode.getDisplayTitle());
        
        try {
            // First, search for the anime to get the correct URL structure
            String animeUrl = findAnimeUrl(episode);
            if (animeUrl == null) {
                logger.warn("Could not find anime URL for: {}", episode.getDisplayTitle());
                return List.of();
            }
            
            // Get the episode page
            String episodeUrl = buildEpisodeUrl(animeUrl, episode.number());
            Document episodePage = fetchPage(episodeUrl);
            
            // Extract stream links from the page
            List<StreamLink> links = extractStreamLinks(episodePage, episode);
            
            logger.info("Found {} stream links for episode: {}", links.size(), episode.getDisplayTitle());
            return links;
            
        } catch (Exception e) {
            logger.error("Failed to resolve links for episode: {}", episode.getDisplayTitle(), e);
            return List.of();
        }
    }
    
    @Override
    public StreamLink resolveBest(Episode episode) {
        List<StreamLink> links = resolve(episode);
        if (links.isEmpty()) {
            throw new RuntimeException("No stream links available for episode: " + episode.getDisplayTitle());
        }
        
        // Prefer higher quality links (720p > 480p > 360p)
        return links.stream()
                .filter(link -> "720p".equals(link.quality()))
                .findFirst()
                .orElse(links.stream()
                        .filter(link -> "480p".equals(link.quality()))
                        .findFirst()
                        .orElse(links.get(0)));
    }
    
    @Override
    public boolean canResolve(Episode episode) {
        // HiAnime resolver can attempt to resolve any episode
        return episode != null && episode.animeId() != null;
    }
    
    private String findAnimeUrl(Episode episode) throws IOException, InterruptedException {
        // Extract anime title from episode for search
        String searchQuery = extractAnimeTitle(episode);
        if (searchQuery == null) {
            return null;
        }
        
        // Search HiAnime for the anime
        String searchUrl = baseUrl + "/search?keyword=" + 
                java.net.URLEncoder.encode(searchQuery, "UTF-8");
        
        Document searchPage = fetchPage(searchUrl);
        Elements animeLinks = searchPage.select("a[href*='/watch/']");
        
        if (!animeLinks.isEmpty()) {
            String href = animeLinks.first().attr("href");
            return href.startsWith("http") ? href : baseUrl + href;
        }
        
        return null;
    }
    
    private String extractAnimeTitle(Episode episode) {
        // First, try the animeTitle field if available
        if (episode.animeTitle() != null && !episode.animeTitle().trim().isEmpty() && 
            !"Unknown".equals(episode.animeTitle())) {
            return episode.animeTitle().trim();
        }
        
        // Try to extract anime title from episode ID or title
        // This is a heuristic approach - may need refinement
        String animeId = episode.animeId();
        
        if (animeId != null && !animeId.trim().isEmpty()) {
            // Remove episode-specific parts from ID
            String title = animeId.replaceAll("_ep_\\d+", "")
                          .replaceAll("-episode-\\d+", "")
                          .replaceAll("\\d+$", "")
                          .trim();
            
            if (!title.isEmpty()) {
                return title;
            }
        }
        
        // Fallback: try to extract from episode title
        String episodeTitle = episode.title();
        if (episodeTitle != null && episodeTitle.contains(":")) {
            // Many episode titles are in format "Anime Name: Episode Title"
            String[] parts = episodeTitle.split(":", 2);
            if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                return parts[0].trim();
            }
        }
        
        logger.warn("Could not extract anime title from episode: {}", episode.getDisplayTitle());
        return null;
    }
    
    private String buildEpisodeUrl(String animeUrl, int episodeNumber) {
        // HiAnime typically uses format: /watch/anime-name-episode-X
        if (animeUrl.contains("/watch/")) {
            return animeUrl + "-episode-" + episodeNumber;
        } else {
            return animeUrl + "/watch/episode-" + episodeNumber;
        }
    }
    
    private Document fetchPage(String url) throws IOException, InterruptedException {
        logger.debug("Fetching page: {}", url);
        
        // Add a small delay to avoid being detected as a bot
        Thread.sleep(1000 + (long)(Math.random() * 2000)); // 1-3 second delay
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("DNT", "1")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1")
                .header("Cache-Control", "max-age=0")
                .header("Referer", baseUrl)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        logger.debug("Response status: {} for URL: {}", response.statusCode(), url);
        
        if (response.statusCode() == 403) {
            throw new IOException("Access forbidden (403) - likely blocked by anti-bot protection: " + url);
        } else if (response.statusCode() == 429) {
            throw new IOException("Rate limited (429) - too many requests: " + url);
        } else if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for URL: " + url);
        }
        
        return Jsoup.parse(response.body());
    }
    
    private List<StreamLink> extractStreamLinks(Document page, Episode episode) {
        List<StreamLink> links = new ArrayList<>();
        
        // Method 1: Look for JSON data in script tags
        links.addAll(extractFromScriptTags(page));
        
        // Method 2: Look for iframe sources
        links.addAll(extractFromIframes(page));
        
        // Method 3: Look for direct video sources
        links.addAll(extractFromVideoTags(page));
        
        return links;
    }
    
    private List<StreamLink> extractFromScriptTags(Document page) {
        List<StreamLink> links = new ArrayList<>();
        
        Elements scripts = page.select("script");
        for (Element script : scripts) {
            String content = script.html();
            
            // Look for JSON data patterns
            Matcher jsonMatcher = JSON_PATTERN.matcher(content);
            if (jsonMatcher.find()) {
                try {
                    String jsonStr = jsonMatcher.group(1);
                    JsonNode json = objectMapper.readTree(jsonStr);
                    links.addAll(parseJsonForStreams(json));
                } catch (Exception e) {
                    logger.debug("Failed to parse JSON from script tag", e);
                }
            }
            
            // Look for direct stream URL patterns
            Matcher urlMatcher = STREAM_URL_PATTERN.matcher(content);
            while (urlMatcher.find()) {
                String url = urlMatcher.group(1);
                if (isValidStreamUrl(url)) {
                    links.add(createStreamLink(url));
                }
            }
        }
        
        return links;
    }
    
    private List<StreamLink> extractFromIframes(Document page) {
        List<StreamLink> links = new ArrayList<>();
        
        Elements iframes = page.select("iframe[src]");
        for (Element iframe : iframes) {
            String src = iframe.attr("src");
            if (isValidStreamUrl(src)) {
                links.add(createStreamLink(src));
            }
        }
        
        return links;
    }
    
    private List<StreamLink> extractFromVideoTags(Document page) {
        List<StreamLink> links = new ArrayList<>();
        
        Elements videos = page.select("video source[src], video[src]");
        for (Element video : videos) {
            String src = video.attr("src");
            if (isValidStreamUrl(src)) {
                links.add(createStreamLink(src));
            }
        }
        
        return links;
    }
    
    private List<StreamLink> parseJsonForStreams(JsonNode json) {
        List<StreamLink> links = new ArrayList<>();
        
        // Recursively search JSON for stream URLs
        parseJsonNode(json, links);
        
        return links;
    }
    
    private void parseJsonNode(JsonNode node, List<StreamLink> links) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                if (value.isTextual() && isValidStreamUrl(value.asText())) {
                    StreamLink link = createStreamLink(value.asText());
                    if (key.contains("720") || key.contains("hd")) {
                        link = new StreamLink(link.url(), "720p", link.format(), link.source(), link.isDirectLink());
                    } else if (key.contains("480")) {
                        link = new StreamLink(link.url(), "480p", link.format(), link.source(), link.isDirectLink());
                    }
                    links.add(link);
                } else {
                    parseJsonNode(value, links);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                parseJsonNode(item, links);
            }
        }
    }
    
    private boolean isValidStreamUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        url = url.toLowerCase();
        return (url.startsWith("http") || url.startsWith("//")) &&
               (url.contains(".mp4") || url.contains(".m3u8") || 
                url.contains(".ts") || url.contains("stream"));
    }
    
    private StreamLink createStreamLink(String url) {
        String quality = "unknown";
        String format = "mp4";
        
        if (url.contains(".m3u8")) {
            format = "hls";
        } else if (url.contains(".ts")) {
            format = "ts";
        }
        
        // Try to detect quality from URL
        if (url.contains("720") || url.contains("hd")) {
            quality = "720p";
        } else if (url.contains("480")) {
            quality = "480p";
        } else if (url.contains("360")) {
            quality = "360p";
        }
        
        return new StreamLink(url, quality, format, "hianime", true);
    }
}

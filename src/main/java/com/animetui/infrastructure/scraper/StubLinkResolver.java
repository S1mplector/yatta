package com.animetui.infrastructure.scraper;

import com.animetui.domain.model.Episode;
import com.animetui.domain.model.StreamLink;
import com.animetui.domain.port.LinkResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Stub implementation of LinkResolver for testing and development.
 * Returns placeholder stream links until real scraping is implemented.
 */
public class StubLinkResolver implements LinkResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(StubLinkResolver.class);
    
    @Override
    public List<StreamLink> resolve(Episode episode) {
        logger.info("Resolving links for episode: {}", episode.getDisplayTitle());
        
        // Return stub links for testing
        return List.of(
            new StreamLink(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "720p",
                "mp4",
                "stub-source",
                true
            ),
            new StreamLink(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "480p",
                "mp4",
                "stub-source",
                true
            )
        );
    }
    
    @Override
    public StreamLink resolveBest(Episode episode) {
        List<StreamLink> links = resolve(episode);
        if (links.isEmpty()) {
            throw new RuntimeException("No stream links available for episode: " + episode.getDisplayTitle());
        }
        
        // Return the first link (highest quality in our stub implementation)
        return links.get(0);
    }
    
    @Override
    public boolean canResolve(Episode episode) {
        // Stub resolver can "resolve" any episode
        return true;
    }
}

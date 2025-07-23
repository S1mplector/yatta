package com.animetui.infrastructure.scraper;

import com.animetui.domain.model.Episode;
import com.animetui.domain.model.StreamLink;
import com.animetui.domain.port.LinkResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * LinkResolver that tries a primary resolver first, then falls back to a secondary resolver.
 * Useful for graceful degradation when real streaming sources are unavailable.
 */
public class FallbackLinkResolver implements LinkResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(FallbackLinkResolver.class);
    
    private final LinkResolver primary;
    private final LinkResolver fallback;
    
    public FallbackLinkResolver(LinkResolver primary, LinkResolver fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }
    
    @Override
    public List<StreamLink> resolve(Episode episode) {
        logger.debug("Attempting to resolve links with primary resolver: {}", 
                    primary.getClass().getSimpleName());
        
        try {
            List<StreamLink> links = primary.resolve(episode);
            if (!links.isEmpty()) {
                logger.info("Primary resolver found {} links for episode: {}", 
                           links.size(), episode.getDisplayTitle());
                return links;
            }
        } catch (Exception e) {
            logger.warn("Primary resolver failed for episode: {} - {}", 
                       episode.getDisplayTitle(), e.getMessage());
        }
        
        logger.info("Falling back to secondary resolver: {}", 
                   fallback.getClass().getSimpleName());
        
        try {
            List<StreamLink> links = fallback.resolve(episode);
            logger.info("Fallback resolver found {} links for episode: {}", 
                       links.size(), episode.getDisplayTitle());
            return links;
        } catch (Exception e) {
            logger.error("Both primary and fallback resolvers failed for episode: {}", 
                        episode.getDisplayTitle(), e);
            throw new RuntimeException("All link resolvers failed", e);
        }
    }
    
    @Override
    public StreamLink resolveBest(Episode episode) {
        List<StreamLink> links = resolve(episode);
        if (links.isEmpty()) {
            throw new RuntimeException("No stream links available for episode: " + episode.getDisplayTitle());
        }
        
        // Use the same quality preference logic as individual resolvers
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
        return primary.canResolve(episode) || fallback.canResolve(episode);
    }
}

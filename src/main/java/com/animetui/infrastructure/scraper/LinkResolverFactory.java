package com.animetui.infrastructure.scraper;

import com.animetui.domain.port.ConfigPort;
import com.animetui.domain.port.LinkResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating LinkResolver implementations based on configuration.
 * Allows switching between different streaming sources via configuration.
 */
public class LinkResolverFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkResolverFactory.class);
    
    public static LinkResolver create(ConfigPort config) {
        String resolverType = config.getString("linkresolver.type", "stub");
        boolean useFallback = config.getBoolean("linkresolver.fallback.enabled", true);
        
        logger.info("Creating LinkResolver of type: {} (fallback: {})", resolverType, useFallback);
        
        LinkResolver primary = switch (resolverType.toLowerCase()) {
            case "hianime" -> createHiAnimeResolver(config);
            case "stub" -> createStubResolver();
            default -> {
                logger.warn("Unknown LinkResolver type '{}', falling back to stub", resolverType);
                yield createStubResolver();
            }
        };
        
        // If fallback is enabled and we're not already using stub, wrap with fallback
        if (useFallback && !"stub".equals(resolverType.toLowerCase())) {
            LinkResolver fallback = createStubResolver();
            return new FallbackLinkResolver(primary, fallback);
        }
        
        return primary;
    }
    
    private static LinkResolver createHiAnimeResolver(ConfigPort config) {
        String baseUrl = config.getString("linkresolver.hianime.baseUrl", "https://hianime.to");
        logger.info("Creating HiAnime LinkResolver with base URL: {}", baseUrl);
        return new HiAnimeLinkResolver(baseUrl);
    }
    
    private static LinkResolver createStubResolver() {
        // Create fallback resolver - use realistic mock for better development experience
        LinkResolver fallbackResolver = new RealisticMockLinkResolver();
        logger.info("Creating Realistic Mock LinkResolver for testing");
        return fallbackResolver;
    }
}

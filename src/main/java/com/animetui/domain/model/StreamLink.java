package com.animetui.domain.model;

/**
 * Domain model representing a streamable video link for an episode.
 * Immutable record following domain-driven design principles.
 */
public record StreamLink(
    String url,
    String quality,
    String format,
    String source,
    boolean isDirectLink
) {
    public StreamLink {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Stream URL cannot be null or blank");
        }
        if (quality == null || quality.isBlank()) {
            throw new IllegalArgumentException("Quality cannot be null or blank");
        }
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Format cannot be null or blank");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Source cannot be null or blank");
        }
    }
    
    /**
     * Get a display string for the stream link.
     */
    public String getDisplayString() {
        return String.format("%s (%s) - %s", quality, format, source);
    }
    
    /**
     * Check if this is a high-quality stream (720p or higher).
     */
    public boolean isHighQuality() {
        return quality.contains("720p") || quality.contains("1080p") || 
               quality.contains("1440p") || quality.contains("4K");
    }
}

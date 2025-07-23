package com.animetui.domain.model;

/**
 * Domain model representing a source site for anime content.
 * Immutable record following domain-driven design principles.
 */
public record SourceSite(
    String id,
    String name,
    String baseUrl,
    boolean isActive
) {
    public SourceSite {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Source site ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Source site name cannot be null or blank");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or blank");
        }
    }
}

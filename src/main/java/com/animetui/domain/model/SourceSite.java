package com.animetui.domain.model;

/**
 * Domain model representing a source site for anime content.
 * Immutable record following domain-driven design principles.
 */
public enum SourceSite {
    HIANIME("HiAnime", "https://hianime.to"),
    GOGOANIME("GogoAnime", "https://gogoanime.pe"),
    ANIWATCH("AniWatch", "https://aniwatch.to"),
    STUB_SOURCE("stub-source", "https://example.com"),
    MOCK_REALISTIC("mock-realistic", "https://mock-streaming.example.com");

    private final String id;
    private final String name;
    private final String baseUrl;

    SourceSite(String name, String baseUrl) {
        this.id = name().toLowerCase();
        this.name = name;
        this.baseUrl = baseUrl;
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or blank");
        }
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}

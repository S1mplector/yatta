package com.animetui.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnimeTest {
    
    @Test
    void shouldCreateValidAnime() {
        // Given
        String id = "123";
        String title = "Test Anime";
        String synopsis = "A test anime";
        String imageUrl = "http://example.com/image.jpg";
        int episodeCount = 12;
        String status = "Completed";
        LocalDate airingDate = LocalDate.of(2023, 1, 1);
        List<String> genres = List.of("Action", "Adventure");
        
        // When
        Anime anime = new Anime(id, title, synopsis, imageUrl, episodeCount, status, airingDate, genres);
        
        // Then
        assertEquals(id, anime.id());
        assertEquals(title, anime.title());
        assertEquals(synopsis, anime.synopsis());
        assertEquals(imageUrl, anime.imageUrl());
        assertEquals(episodeCount, anime.episodeCount());
        assertEquals(status, anime.status());
        assertEquals(airingDate, anime.airingDate());
        assertEquals(genres, anime.genres());
    }
    
    @Test
    void shouldThrowExceptionForNullId() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Anime(null, "Title", "Synopsis", "url", 12, "Completed", LocalDate.now(), List.of())
        );
    }
    
    @Test
    void shouldThrowExceptionForBlankId() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Anime("", "Title", "Synopsis", "url", 12, "Completed", LocalDate.now(), List.of())
        );
    }
    
    @Test
    void shouldThrowExceptionForNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Anime("123", null, "Synopsis", "url", 12, "Completed", LocalDate.now(), List.of())
        );
    }
    
    @Test
    void shouldThrowExceptionForBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Anime("123", "", "Synopsis", "url", 12, "Completed", LocalDate.now(), List.of())
        );
    }
    
    @Test
    void shouldThrowExceptionForNegativeEpisodeCount() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Anime("123", "Title", "Synopsis", "url", -1, "Completed", LocalDate.now(), List.of())
        );
    }
    
    @Test
    void shouldReturnTrueForAiringStatus() {
        Anime anime = new Anime("123", "Title", "Synopsis", "url", 12, "Airing", LocalDate.now(), List.of());
        assertTrue(anime.isAiring());
    }
    
    @Test
    void shouldReturnTrueForCompletedStatus() {
        Anime anime = new Anime("123", "Title", "Synopsis", "url", 12, "Completed", LocalDate.now(), List.of());
        assertTrue(anime.isCompleted());
    }
    
    @Test
    void shouldHandleNullGenres() {
        Anime anime = new Anime("123", "Title", "Synopsis", "url", 12, "Completed", LocalDate.now(), null);
        assertTrue(anime.genres().isEmpty());
    }
    
    @Test
    void shouldCreateDefensiveCopyOfGenres() {
        List<String> originalGenres = List.of("Action", "Adventure");
        Anime anime = new Anime("123", "Title", "Synopsis", "url", 12, "Completed", LocalDate.now(), originalGenres);
        
        // The genres should be a copy, not the same reference
        assertNotSame(originalGenres, anime.genres());
        assertEquals(originalGenres, anime.genres());
    }
}

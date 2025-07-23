package com.animetui.application;

import com.animetui.application.dto.AnimeDto;
import com.animetui.domain.model.Anime;
import com.animetui.domain.port.AnimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FetchCatalogUseCaseTest {
    
    @Mock
    private AnimeRepository animeRepository;
    
    private FetchCatalogUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new FetchCatalogUseCase(animeRepository);
    }
    
    @Test
    void shouldFetchPopularAnimeWithDefaultLimit() {
        // Given
        List<Anime> mockAnime = List.of(
            new Anime("1", "Anime 1", "Synopsis 1", "url1", 12, "Completed", LocalDate.now(), List.of("Action")),
            new Anime("2", "Anime 2", "Synopsis 2", "url2", 24, "Airing", LocalDate.now(), List.of("Drama"))
        );
        when(animeRepository.listPopular(20)).thenReturn(mockAnime);
        
        // When
        List<AnimeDto> result = useCase.execute();
        
        // Then
        assertEquals(2, result.size());
        assertEquals("Anime 1", result.get(0).title());
        assertEquals("Anime 2", result.get(1).title());
        verify(animeRepository).listPopular(20);
    }
    
    @Test
    void shouldFetchPopularAnimeWithCustomLimit() {
        // Given
        List<Anime> mockAnime = List.of(
            new Anime("1", "Anime 1", "Synopsis 1", "url1", 12, "Completed", LocalDate.now(), List.of("Action"))
        );
        when(animeRepository.listPopular(5)).thenReturn(mockAnime);
        
        // When
        List<AnimeDto> result = useCase.execute(5);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("Anime 1", result.get(0).title());
        verify(animeRepository).listPopular(5);
    }
    
    @Test
    void shouldThrowExceptionForInvalidLimit() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(0));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(-1));
    }
    
    @Test
    void shouldFetchCurrentSeasonAnime() {
        // Given
        List<Anime> mockAnime = List.of(
            new Anime("1", "Current Anime", "Synopsis", "url", 12, "Airing", LocalDate.now(), List.of("Action"))
        );
        when(animeRepository.getCurrentSeason(10)).thenReturn(mockAnime);
        
        // When
        List<AnimeDto> result = useCase.executeCurrentSeason(10);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("Current Anime", result.get(0).title());
        verify(animeRepository).getCurrentSeason(10);
    }
    
    @Test
    void shouldConvertAnimeToDto() {
        // Given
        LocalDate airingDate = LocalDate.of(2023, 1, 15);
        List<Anime> mockAnime = List.of(
            new Anime("123", "Test Anime", "Test Synopsis", "http://image.url", 
                     24, "Completed", airingDate, List.of("Action", "Adventure"))
        );
        when(animeRepository.listPopular(20)).thenReturn(mockAnime);
        
        // When
        List<AnimeDto> result = useCase.execute();
        
        // Then
        AnimeDto dto = result.get(0);
        assertEquals("123", dto.id());
        assertEquals("Test Anime", dto.title());
        assertEquals("Test Synopsis", dto.synopsis());
        assertEquals("http://image.url", dto.imageUrl());
        assertEquals(24, dto.episodeCount());
        assertEquals("Completed", dto.status());
        assertEquals("2023-01-15", dto.airingDate());
        assertEquals(List.of("Action", "Adventure"), dto.genres());
    }
}

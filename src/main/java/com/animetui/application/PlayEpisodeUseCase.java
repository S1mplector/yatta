package com.animetui.application;

import com.animetui.domain.model.Episode;
import com.animetui.domain.model.StreamLink;
import com.animetui.domain.port.LinkResolver;
import com.animetui.domain.port.MediaPlayerPort;

/**
 * Use case for playing an episode.
 * Orchestrates link resolution and media player to fulfill playback requests.
 */
public class PlayEpisodeUseCase {
    
    private final LinkResolver linkResolver;
    private final MediaPlayerPort mediaPlayer;
    
    public PlayEpisodeUseCase(LinkResolver linkResolver, MediaPlayerPort mediaPlayer) {
        this.linkResolver = linkResolver;
        this.mediaPlayer = mediaPlayer;
    }
    
    /**
     * Play the given episode using the best available stream link.
     */
    public void execute(Episode episode) {
        if (episode == null) {
            throw new IllegalArgumentException("Episode cannot be null");
        }
        
        if (!mediaPlayer.isAvailable()) {
            throw new RuntimeException("Media player is not available on this system");
        }
        
        try {
            StreamLink link = linkResolver.resolveBest(episode);
            mediaPlayer.play(link);
        } catch (Exception e) {
            throw new RuntimeException("Failed to play episode: " + episode.getDisplayTitle(), e);
        }
    }
    
    /**
     * Play the episode with a specific stream link quality preference.
     */
    public void execute(Episode episode, String preferredQuality) {
        if (episode == null) {
            throw new IllegalArgumentException("Episode cannot be null");
        }
        if (preferredQuality == null || preferredQuality.isBlank()) {
            throw new IllegalArgumentException("Preferred quality cannot be null or blank");
        }
        
        if (!mediaPlayer.isAvailable()) {
            throw new RuntimeException("Media player is not available on this system");
        }
        
        try {
            var links = linkResolver.resolve(episode);
            StreamLink selectedLink = links.stream()
                    .filter(link -> link.quality().equalsIgnoreCase(preferredQuality))
                    .findFirst()
                    .orElse(linkResolver.resolveBest(episode)); // Fallback to best quality
                    
            mediaPlayer.play(selectedLink);
        } catch (Exception e) {
            throw new RuntimeException("Failed to play episode: " + episode.getDisplayTitle(), e);
        }
    }
    
    /**
     * Stop the currently playing episode.
     */
    public void stop() {
        mediaPlayer.stop();
    }
    
    /**
     * Pause the currently playing episode.
     */
    public void pause() {
        mediaPlayer.pause();
    }
    
    /**
     * Resume the paused episode.
     */
    public void resume() {
        mediaPlayer.resume();
    }
}

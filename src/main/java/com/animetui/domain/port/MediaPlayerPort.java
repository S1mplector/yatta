package com.animetui.domain.port;

import com.animetui.domain.model.StreamLink;

/**
 * Port for controlling external media players.
 * This interface defines the contract for media player implementations.
 */
public interface MediaPlayerPort {
    
    /**
     * Play a video from the given URL.
     * 
     * @param url the video URL to play
     */
    void play(String url);
    
    /**
     * Play a video from the given stream link.
     * 
     * @param streamLink the stream link containing URL and metadata
     */
    void play(StreamLink streamLink);
    
    /**
     * Pause the currently playing video.
     */
    void pause();
    
    /**
     * Resume playback of the paused video.
     */
    void resume();
    
    /**
     * Stop playback and close the player.
     */
    void stop();
    
    /**
     * Check if the player is currently playing.
     * 
     * @return true if playing, false otherwise
     */
    boolean isPlaying();
    
    /**
     * Check if the player is available on the system.
     * 
     * @return true if the player can be launched
     */
    boolean isAvailable();
}

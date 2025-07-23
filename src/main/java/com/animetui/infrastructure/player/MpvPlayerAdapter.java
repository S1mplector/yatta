package com.animetui.infrastructure.player;

import com.animetui.domain.model.StreamLink;
import com.animetui.domain.port.MediaPlayerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Infrastructure implementation of MediaPlayerPort using MPV player.
 * Spawns MPV process to play video content.
 */
public class MpvPlayerAdapter implements MediaPlayerPort {
    
    private static final Logger logger = LoggerFactory.getLogger(MpvPlayerAdapter.class);
    
    private final String playerCommand;
    private final List<String> defaultArgs;
    private Process currentProcess;
    
    public MpvPlayerAdapter(String playerCommand, List<String> args) {
        this.playerCommand = playerCommand != null ? playerCommand : "mpv";
        this.defaultArgs = args != null ? List.copyOf(args) : List.of("--no-terminal");
    }
    
    public MpvPlayerAdapter(String playerCommand) {
        this(playerCommand, null);
    }
    
    public MpvPlayerAdapter() {
        this("mpv");
    }
    
    @Override
    public void play(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank");
        }
        
        stop(); // Stop any currently playing video
        
        try {
            List<String> command = new ArrayList<>();
            command.add(playerCommand);
            command.addAll(defaultArgs);
            command.add(url);
            
            logger.info("Starting media player: {}", String.join(" ", command));
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO(); // Allow player output to show in console
            currentProcess = pb.start();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to start media player: " + playerCommand, e);
        }
    }
    
    @Override
    public void play(StreamLink streamLink) {
        if (streamLink == null) {
            throw new IllegalArgumentException("StreamLink cannot be null");
        }
        
        logger.info("Playing stream: {} ({})", streamLink.getDisplayString(), streamLink.url());
        play(streamLink.url());
    }
    
    @Override
    public void pause() {
        // MPV doesn't support external pause/resume via simple process control
        // This would require IPC or input pipe communication
        logger.warn("Pause functionality requires MPV IPC setup");
    }
    
    @Override
    public void resume() {
        // MPV doesn't support external pause/resume via simple process control
        logger.warn("Resume functionality requires MPV IPC setup");
    }
    
    @Override
    public void stop() {
        if (currentProcess != null && currentProcess.isAlive()) {
            logger.info("Stopping media player");
            currentProcess.destroy();
            try {
                // Give it a moment to terminate gracefully
                if (!currentProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    currentProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                currentProcess.destroyForcibly();
            }
            currentProcess = null;
        }
    }
    
    @Override
    public boolean isPlaying() {
        return currentProcess != null && currentProcess.isAlive();
    }
    
    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(playerCommand, "--version");
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = pb.start();
            boolean exited = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            return exited && process.exitValue() == 0;
        } catch (Exception e) {
            logger.debug("Media player not available: {}", e.getMessage());
            return false;
        }
    }
}

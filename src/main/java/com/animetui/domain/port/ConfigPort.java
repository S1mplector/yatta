package com.animetui.domain.port;

import java.util.Optional;

/**
 * Port for accessing application configuration.
 * This interface defines the contract for configuration implementations.
 */
public interface ConfigPort {
    
    /**
     * Get a configuration value as a string.
     * 
     * @param key the configuration key
     * @return the configuration value if present
     */
    Optional<String> getString(String key);
    
    /**
     * Get a configuration value as a string with a default value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the configuration value or default
     */
    String getString(String key, String defaultValue);
    
    /**
     * Get a configuration value as an integer.
     * 
     * @param key the configuration key
     * @return the configuration value if present and valid
     */
    Optional<Integer> getInt(String key);
    
    /**
     * Get a configuration value as an integer with a default value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the configuration value or default
     */
    int getInt(String key, int defaultValue);
    
    /**
     * Get a configuration value as a boolean.
     * 
     * @param key the configuration key
     * @return the configuration value if present and valid
     */
    Optional<Boolean> getBoolean(String key);
    
    /**
     * Get a configuration value as a boolean with a default value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the configuration value or default
     */
    boolean getBoolean(String key, boolean defaultValue);
}

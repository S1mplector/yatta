package com.animetui.infrastructure.config;

import com.animetui.domain.port.ConfigPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Infrastructure implementation of ConfigPort.
 * Loads configuration from application.properties and environment variables.
 */
public class AppConfig implements ConfigPort {
    
    private final Properties properties;
    
    public AppConfig() {
        this.properties = loadProperties();
    }
    
    /**
     * Load configuration from multiple sources in order of precedence:
     * 1. Environment variables
     * 2. application.properties
     * 3. Default values
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        
        // Load from application.properties
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            // Log warning but continue with defaults
            System.err.println("Warning: Could not load application.properties: " + e.getMessage());
        }
        
        return props;
    }
    
    @Override
    public Optional<String> getString(String key) {
        // Check environment variables first (with uppercase and dots replaced by underscores)
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            return Optional.of(envValue);
        }
        
        // Check system properties
        String sysProp = System.getProperty(key);
        if (sysProp != null) {
            return Optional.of(sysProp);
        }
        
        // Check application.properties
        String propValue = properties.getProperty(key);
        return Optional.ofNullable(propValue);
    }
    
    @Override
    public String getString(String key, String defaultValue) {
        return getString(key).orElse(defaultValue);
    }
    
    @Override
    public Optional<Integer> getInt(String key) {
        return getString(key).map(value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer value for key '" + key + "': " + value);
            }
        });
    }
    
    @Override
    public int getInt(String key, int defaultValue) {
        return getInt(key).orElse(defaultValue);
    }
    
    @Override
    public Optional<Boolean> getBoolean(String key) {
        return getString(key).map(value -> {
            if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
                return true;
            } else if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
                return false;
            } else {
                throw new IllegalArgumentException("Invalid boolean value for key '" + key + "': " + value);
            }
        });
    }
    
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key).orElse(defaultValue);
    }
    
    /**
     * Static factory method to create and load configuration.
     */
    public static AppConfig load() {
        return new AppConfig();
    }
}

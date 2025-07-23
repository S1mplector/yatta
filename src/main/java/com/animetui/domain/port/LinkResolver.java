package com.animetui.domain.port;

import com.animetui.domain.model.Episode;
import com.animetui.domain.model.StreamLink;

import java.util.List;

/**
 * Port for resolving streamable video links for episodes.
 * This interface defines the contract for link resolution implementations.
 */
public interface LinkResolver {
    
    /**
     * Resolve all available stream links for a given episode.
     * 
     * @param episode the episode to resolve links for
     * @return list of available stream links, ordered by quality (best first)
     */
    List<StreamLink> resolve(Episode episode);
    
    /**
     * Resolve the best quality stream link for a given episode.
     * 
     * @param episode the episode to resolve the best link for
     * @return the highest quality stream link available
     * @throws RuntimeException if no links are available
     */
    StreamLink resolveBest(Episode episode);
    
    /**
     * Check if the resolver can handle links for the given episode.
     * 
     * @param episode the episode to check
     * @return true if this resolver can handle the episode
     */
    boolean canResolve(Episode episode);
}

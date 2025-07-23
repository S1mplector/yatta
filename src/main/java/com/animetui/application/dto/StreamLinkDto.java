package com.animetui.application.dto;

/**
 * Data Transfer Object for stream link information.
 * Lightweight representation for presentation layer.
 */
public record StreamLinkDto(
    String url,
    String quality,
    String format,
    String source,
    boolean isDirectLink
) {
}

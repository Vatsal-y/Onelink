package com.snaplink.exception;

/**
 * Thrown when a short link has passed its expiry timestamp.
 * Maps to HTTP 410 Gone.
 */
public class LinkExpiredException extends RuntimeException {

    private final String shortCode;

    public LinkExpiredException(String shortCode) {
        super("Short link has expired: " + shortCode);
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}

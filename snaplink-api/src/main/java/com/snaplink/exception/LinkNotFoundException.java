package com.snaplink.exception;

/**
 * Thrown when a short code cannot be found in the database.
 * Maps to HTTP 404 Not Found.
 */
public class LinkNotFoundException extends RuntimeException {

    private final String shortCode;

    public LinkNotFoundException(String shortCode) {
        super("Short link not found: " + shortCode);
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}

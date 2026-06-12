package com.snaplink.exception;

/**
 * Thrown when a custom alias conflicts with an existing short code.
 * Maps to HTTP 409 Conflict.
 */
public class AliasConflictException extends RuntimeException {

    private final String alias;

    public AliasConflictException(String alias) {
        super("Alias already taken: " + alias);
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}

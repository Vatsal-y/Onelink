package com.snaplink.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/shorten}.
 */
public class ShortenRequest {

    @NotBlank(message = "longUrl is required")
    @org.hibernate.validator.constraints.URL(message = "longUrl must be a valid URL")
    private String longUrl;

    @Size(min = 3, max = 30, message = "Alias must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Alias may only contain letters, digits, and hyphens")
    private String alias;

    @Pattern(regexp = "^(1h|1d|7d|30d|never)$", message = "expiresIn must be one of: 1h, 1d, 7d, 30d, never")
    private String expiresIn;

    public ShortenRequest() {}

    public ShortenRequest(String longUrl, String alias, String expiresIn) {
        this.longUrl = longUrl;
        this.alias = alias;
        this.expiresIn = expiresIn;
    }

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getExpiresIn() { return expiresIn; }
    public void setExpiresIn(String expiresIn) { this.expiresIn = expiresIn; }
}

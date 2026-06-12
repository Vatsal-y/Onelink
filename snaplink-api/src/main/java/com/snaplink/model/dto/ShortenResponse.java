package com.snaplink.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response body for {@code POST /api/shorten}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortenResponse {

    private String shortUrl;
    private String shortCode;
    private String qrCodeUrl;
    private String expiresAt;
    private String createdAt;

    public ShortenResponse() {}

    public ShortenResponse(String shortUrl, String shortCode, String qrCodeUrl,
                           String expiresAt, String createdAt) {
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.qrCodeUrl = qrCodeUrl;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

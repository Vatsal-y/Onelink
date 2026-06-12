package com.snaplink.model;

/**
 * Payload published to SQS on every successful redirect.
 * Consumed by {@link com.snaplink.consumer.ClickEventConsumer}.
 */
public record ClickEvent(
        String shortCode,
        String timestamp,      // ISO 8601
        String ipHash,         // SHA-256 of requester IP
        String userAgent,
        String referer,
        String country,        // 2-letter ISO code (derived from IP geo)
        String device          // "mobile" | "desktop" | "bot"
) {}

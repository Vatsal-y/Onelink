package com.snaplink.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB entity for the {@code snaplink-analytics} table.
 *
 * <p>Partition key: {@code shortCodeDate} — composite key "{shortCode}#{YYYY-MM-DD}"
 * <p>Sort key: {@code clickId} — UUID per click event
 *
 * <p>This design enables efficient time-range queries per short code:
 * query all clicks for a given code on a specific day with a single
 * partition key lookup.
 */
@DynamoDbBean
public class AnalyticsEntity {

    private String shortCodeDate;   // PK — "abc123#2026-06-12"
    private String clickId;         // SK — UUID
    private String timestamp;       // ISO 8601
    private String country;         // 2-letter ISO country code
    private String device;          // "mobile" | "desktop" | "bot"
    private String referer;         // Referring domain
    private String ipHash;          // SHA-256 of IP address

    public AnalyticsEntity() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("shortCodeDate")
    public String getShortCodeDate() { return shortCodeDate; }
    public void setShortCodeDate(String shortCodeDate) { this.shortCodeDate = shortCodeDate; }

    @DynamoDbSortKey
    @DynamoDbAttribute("clickId")
    public String getClickId() { return clickId; }
    public void setClickId(String clickId) { this.clickId = clickId; }

    @DynamoDbAttribute("timestamp")
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @DynamoDbAttribute("country")
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    @DynamoDbAttribute("device")
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    @DynamoDbAttribute("referer")
    public String getReferer() { return referer; }
    public void setReferer(String referer) { this.referer = referer; }

    @DynamoDbAttribute("ipHash")
    public String getIpHash() { return ipHash; }
    public void setIpHash(String ipHash) { this.ipHash = ipHash; }
}

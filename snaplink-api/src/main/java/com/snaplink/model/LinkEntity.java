package com.snaplink.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

/**
 * DynamoDB entity for the {@code snaplink-links} table.
 *
 * <p>Partition key: {@code shortCode} (6-char base-62 or custom alias).
 * <p>GSI {@code userId-createdAt-index}: PK = userId, SK = createdAt.
 */
@DynamoDbBean
public class LinkEntity {

    private String shortCode;
    private String longUrl;
    private String userId;
    private String createdAt;
    private Long expiresAt;     // Unix epoch seconds — DynamoDB TTL attribute
    private Long clickCount;
    private String qrCodeKey;   // S3 object key: "qr/{shortCode}.png"

    public LinkEntity() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("shortCode")
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    @DynamoDbAttribute("longUrl")
    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    @DynamoDbSecondaryPartitionKey(indexNames = "userId-createdAt-index")
    @DynamoDbAttribute("userId")
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSecondarySortKey(indexNames = "userId-createdAt-index")
    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbAttribute("expiresAt")
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    @DynamoDbAttribute("clickCount")
    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    @DynamoDbAttribute("qrCodeKey")
    public String getQrCodeKey() { return qrCodeKey; }
    public void setQrCodeKey(String qrCodeKey) { this.qrCodeKey = qrCodeKey; }
}

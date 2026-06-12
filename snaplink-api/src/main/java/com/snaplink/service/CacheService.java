package com.snaplink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Manual cache-aside implementation for short URL lookups.
 *
 * <p>Key format: {@code short:{code}} → long URL string.
 * <p>TTL: 1 hour per cache entry.
 *
 * <p>This is implemented manually (not via {@code @Cacheable}) to allow
 * fine-grained TTL control and explicit invalidation on link deletion.
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    private static final String KEY_PREFIX = "short:";
    private static final long CACHE_TTL_HOURS = 1;

    private final RedisTemplate<String, String> redisTemplate;

    public CacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempts to retrieve the cached long URL for a short code.
     *
     * @param shortCode the short code
     * @return the cached long URL, or {@code null} on cache miss
     */
    public String get(String shortCode) {
        try {
            String key = KEY_PREFIX + shortCode;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache HIT: {}", shortCode);
            } else {
                log.debug("Cache MISS: {}", shortCode);
            }
            return value;
        } catch (Exception e) {
            log.warn("Redis get failed for {}: {}", shortCode, e.getMessage());
            return null; // Fail open — fall back to DynamoDB
        }
    }

    /**
     * Stores a long URL in the cache with a 1-hour TTL.
     */
    public void put(String shortCode, String longUrl) {
        try {
            String key = KEY_PREFIX + shortCode;
            redisTemplate.opsForValue().set(key, longUrl, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cached: {} → {}", shortCode, longUrl);
        } catch (Exception e) {
            log.warn("Redis put failed for {}: {}", shortCode, e.getMessage());
            // Non-fatal — the next request will miss cache and fetch from DynamoDB
        }
    }

    /**
     * Invalidates the cache entry for a short code.
     * Called when a link is deleted.
     */
    public void invalidate(String shortCode) {
        try {
            String key = KEY_PREFIX + shortCode;
            redisTemplate.delete(key);
            log.debug("Cache invalidated: {}", shortCode);
        } catch (Exception e) {
            log.warn("Redis delete failed for {}: {}", shortCode, e.getMessage());
        }
    }
}

package com.snaplink.service;

import com.snaplink.exception.LinkExpiredException;
import com.snaplink.exception.LinkNotFoundException;
import com.snaplink.model.LinkEntity;
import com.snaplink.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Handles short link resolution and redirect logic.
 *
 * <p>Implements the cache-aside pattern:
 * <ol>
 *   <li>Check Redis for the long URL</li>
 *   <li>On miss → query DynamoDB, populate cache</li>
 *   <li>Check TTL expiry → 410 Gone if expired</li>
 *   <li>Return the destination URL for redirect</li>
 * </ol>
 */
@Service
public class RedirectService {

    private static final Logger log = LoggerFactory.getLogger(RedirectService.class);

    private final CacheService cacheService;
    private final LinkRepository linkRepository;

    public RedirectService(CacheService cacheService, LinkRepository linkRepository) {
        this.cacheService = cacheService;
        this.linkRepository = linkRepository;
    }

    /**
     * Resolves a short code to its destination URL.
     *
     * @param shortCode the short code from the URL path
     * @return the long URL to redirect to
     * @throws LinkNotFoundException if the code does not exist
     * @throws LinkExpiredException  if the link has expired
     */
    public String resolve(String shortCode) {
        // 1. Try cache first
        String cachedUrl = cacheService.get(shortCode);
        if (cachedUrl != null) {
            log.info("Resolved {} -> {} (Cache HIT 🚀)", shortCode, cachedUrl);
            return cachedUrl;
        }

        log.info("Cache MISS for code: {} - fetching from DynamoDB...", shortCode);

        // 2. Cache miss — fetch from DynamoDB
        LinkEntity link = linkRepository.findByShortCode(shortCode);
        if (link == null) {
            throw new LinkNotFoundException(shortCode);
        }

        // 3. Check expiry
        if (link.getExpiresAt() != null && link.getExpiresAt() < Instant.now().getEpochSecond()) {
            throw new LinkExpiredException(shortCode);
        }

        // 4. Populate cache
        cacheService.put(shortCode, link.getLongUrl());

        log.info("Resolved {} -> {} (Database fetch + Cache updated 💾)", shortCode, link.getLongUrl());
        return link.getLongUrl();
    }

    /**
     * Retrieves the full link entity for a given short code.
     * Used by the analytics controller.
     */
    public LinkEntity getLinkEntity(String shortCode) {
        LinkEntity link = linkRepository.findByShortCode(shortCode);
        if (link == null) {
            throw new LinkNotFoundException(shortCode);
        }
        return link;
    }
}

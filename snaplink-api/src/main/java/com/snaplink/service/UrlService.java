package com.snaplink.service;

import com.snaplink.exception.AliasConflictException;
import com.snaplink.model.LinkEntity;
import com.snaplink.model.dto.ShortenRequest;
import com.snaplink.model.dto.ShortenResponse;
import com.snaplink.repository.LinkRepository;
import com.snaplink.util.Base62Encoder;
import com.snaplink.util.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Core business logic for URL shortening.
 *
 * <p>Handles URL validation, short code generation (random base-62 or custom alias),
 * collision detection, expiry computation, QR code generation, and persistence.
 */
@Service
public class UrlService {

    private static final Logger log = LoggerFactory.getLogger(UrlService.class);
    private static final int MAX_RETRIES = 5;

    private static final Map<String, Duration> EXPIRY_MAP = Map.of(
            "1h", Duration.ofHours(1),
            "1d", Duration.ofDays(1),
            "7d", Duration.ofDays(7),
            "30d", Duration.ofDays(30)
    );

    private final LinkRepository linkRepository;
    private final QrCodeService qrCodeService;
    private final CacheService cacheService;

    @Value("${snaplink.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlService(LinkRepository linkRepository,
                      QrCodeService qrCodeService,
                      CacheService cacheService) {
        this.linkRepository = linkRepository;
        this.qrCodeService = qrCodeService;
        this.cacheService = cacheService;
    }

    /**
     * Creates a new short link.
     *
     * @param request the shorten request (longUrl, optional alias, optional expiresIn)
     * @param userId  the authenticated user's ID (Cognito sub)
     * @return the shorten response with short URL, QR code URL, and timestamps
     */
    public ShortenResponse createShortLink(ShortenRequest request, String userId) {
        // 1. Validate URL
        String validationError = UrlValidator.validate(request.getLongUrl());
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // 2. Determine short code
        String shortCode;
        if (request.getAlias() != null && !request.getAlias().isBlank()) {
            shortCode = request.getAlias();
            if (linkRepository.existsByShortCode(shortCode)) {
                throw new AliasConflictException(shortCode);
            }
        } else {
            shortCode = generateUniqueCode();
        }

        // 3. Compute expiry
        Long expiresAt = computeExpiresAt(request.getExpiresIn());

        // 4. Generate QR code and upload to S3
        String qrCodeKey = qrCodeService.generateAndUpload(shortCode);
        String qrCodeUrl = qrCodeService.getPresignedUrl(qrCodeKey);

        // 5. Build and save entity
        Instant now = Instant.now();
        String createdAt = DateTimeFormatter.ISO_INSTANT.format(now);

        LinkEntity entity = new LinkEntity();
        entity.setShortCode(shortCode);
        entity.setLongUrl(request.getLongUrl());
        entity.setUserId(userId != null ? userId : "anonymous");
        entity.setCreatedAt(createdAt);
        entity.setExpiresAt(expiresAt);
        entity.setClickCount(0L);
        entity.setQrCodeKey(qrCodeKey);

        linkRepository.save(entity);

        // 6. Pre-warm cache
        cacheService.put(shortCode, request.getLongUrl());

        log.info("Created short link: {} → {} (user={})", shortCode, request.getLongUrl(), userId);

        // 7. Build response
        String expiresAtStr = expiresAt != null
                ? DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(expiresAt))
                : null;

        return new ShortenResponse(
                baseUrl + "/" + shortCode,
                shortCode,
                qrCodeUrl,
                expiresAtStr,
                createdAt
        );
    }

    /**
     * Lists all links for a given user.
     */
    public List<LinkEntity> listUserLinks(String userId) {
        return linkRepository.findByUserId(userId);
    }

    /**
     * Deletes a short link and invalidates its cache entry.
     */
    public void deleteLink(String shortCode, String userId) {
        LinkEntity link = linkRepository.findByShortCode(shortCode);
        if (link == null) {
            throw new com.snaplink.exception.LinkNotFoundException(shortCode);
        }
        // Authorization check: only the owner can delete
        if (!link.getUserId().equals(userId) && !"anonymous".equals(link.getUserId())) {
            throw new IllegalArgumentException("You do not own this link");
        }

        linkRepository.delete(shortCode);
        cacheService.invalidate(shortCode);
        log.info("Deleted short link: {} (user={})", shortCode, userId);
    }

    /**
     * Generates a unique 6-char base-62 code with collision retry.
     */
    private String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String code = Base62Encoder.generateRandomCode();
            if (!linkRepository.existsByShortCode(code)) {
                return code;
            }
            log.warn("Short code collision on attempt {}: {}", i + 1, code);
        }
        throw new RuntimeException("Failed to generate unique short code after " + MAX_RETRIES + " attempts");
    }

    /**
     * Computes the Unix epoch expiry timestamp from the expiresIn string.
     *
     * @return Unix epoch seconds, or {@code null} if "never" or not specified
     */
    private Long computeExpiresAt(String expiresIn) {
        if (expiresIn == null || expiresIn.isBlank() || "never".equalsIgnoreCase(expiresIn)) {
            return null;
        }

        Duration duration = EXPIRY_MAP.get(expiresIn);
        if (duration == null) {
            throw new IllegalArgumentException("Invalid expiresIn value: " + expiresIn);
        }

        return Instant.now().plus(duration).getEpochSecond();
    }
}

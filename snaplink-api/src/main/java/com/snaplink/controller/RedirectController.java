package com.snaplink.controller;

import com.snaplink.service.AnalyticsService;
import com.snaplink.service.RedirectService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Handles short link redirects.
 *
 * <p>{@code GET /{code}} performs a 301 Moved Permanently redirect
 * to the original destination URL. After redirecting, fires an
 * asynchronous click event to SQS for analytics processing.
 */
@RestController
public class RedirectController {

    private static final Logger log = LoggerFactory.getLogger(RedirectController.class);

    private final RedirectService redirectService;
    private final AnalyticsService analyticsService;

    public RedirectController(RedirectService redirectService, AnalyticsService analyticsService) {
        this.redirectService = redirectService;
        this.analyticsService = analyticsService;
    }

    /**
     * Resolves a short code and redirects to the destination URL.
     *
     * <p>Returns 301 (Moved Permanently) on success.
     * <p>Returns 404 if the code is not found.
     * <p>Returns 410 if the link has expired.
     */
    @GetMapping("/{code:[a-zA-Z0-9\\-]{3,30}}")
    public ResponseEntity<Void> redirect(
            @PathVariable("code") String code,
            HttpServletRequest request) {

        String destinationUrl = redirectService.resolve(code);

        // Fire analytics event asynchronously — must not block the redirect
        try {
            analyticsService.publishClickEvent(request, code);
        } catch (Exception e) {
            log.warn("Failed to publish click event for {}: {}", code, e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(destinationUrl));

        log.info("Redirecting /{} → {}", code, destinationUrl);
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}

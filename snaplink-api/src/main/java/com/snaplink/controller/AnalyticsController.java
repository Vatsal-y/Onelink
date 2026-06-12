package com.snaplink.controller;

import com.snaplink.model.dto.AnalyticsResponse;
import com.snaplink.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for click analytics.
 *
 * <p>{@code GET /api/analytics/{code}} returns aggregated analytics
 * for a specific short link.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Returns aggregated analytics for a short code.
     *
     * @param code the short code
     * @param days number of days to look back (default 7, max 30)
     */
    @GetMapping("/{code}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable("code") String code,
            @RequestParam(value = "days", defaultValue = "7") int days) {

        if (days < 1) days = 1;
        if (days > 30) days = 30;

        AnalyticsResponse response = analyticsService.getAnalytics(code, days);
        return ResponseEntity.ok(response);
    }
}

package com.snaplink.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snaplink.model.AnalyticsEntity;
import com.snaplink.model.ClickEvent;
import com.snaplink.model.dto.AnalyticsResponse;
import com.snaplink.repository.AnalyticsRepository;
import com.snaplink.repository.LinkRepository;
import com.snaplink.util.IpHasher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Manages click analytics: publishing events to SQS and querying aggregated data.
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    // Simple User-Agent patterns for device detection
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(?i)(mobile|android|iphone|ipad|ipod|blackberry|windows phone|opera mini|opera mobi)");
    private static final Pattern BOT_PATTERN = Pattern.compile(
            "(?i)(bot|crawl|spider|slurp|googlebot|bingbot|yandex|baidu|duckduckbot|facebookexternalhit)");

    private final SqsTemplate sqsTemplate;
    private final AnalyticsRepository analyticsRepository;
    private final LinkRepository linkRepository;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-name:snaplink-click-events}")
    private String queueName;

    public AnalyticsService(SqsTemplate sqsTemplate,
                            AnalyticsRepository analyticsRepository,
                            LinkRepository linkRepository,
                            ObjectMapper objectMapper) {
        this.sqsTemplate = sqsTemplate;
        this.analyticsRepository = analyticsRepository;
        this.linkRepository = linkRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a click event to SQS for asynchronous processing.
     * Called after every successful redirect.
     *
     * @param request   the HTTP request (for extracting headers)
     * @param shortCode the short code that was clicked
     */
    public void publishClickEvent(HttpServletRequest request, String shortCode) {
        try {
            String ip = extractIp(request);
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");

            ClickEvent event = new ClickEvent(
                    shortCode,
                    Instant.now().toString(),
                    IpHasher.hash(ip),
                    userAgent != null ? userAgent : "",
                    referer != null ? referer : "",
                    "XX",  // Country — resolved by consumer or external service
                    detectDevice(userAgent)
            );

            String payload = objectMapper.writeValueAsString(event);
            sqsTemplate.send(queueName, payload);

            log.debug("Published click event for {}", shortCode);
        } catch (Exception e) {
            // Non-fatal — analytics should never block redirects
            log.error("Failed to publish click event for {}: {}", shortCode, e.getMessage());
        }
    }

    /**
     * Retrieves aggregated analytics for a short code over a given number of days.
     *
     * @param shortCode the short code
     * @param days      number of days to look back (default 7)
     * @return aggregated analytics response
     */
    public AnalyticsResponse getAnalytics(String shortCode, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<AnalyticsEntity> events = analyticsRepository
                .findByShortCodeAndDateRange(shortCode, startDate, endDate);

        // Total clicks
        long totalClicks = events.size();

        // Unique clicks (by IP hash)
        long uniqueClicks = events.stream()
                .map(AnalyticsEntity::getIpHash)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // Clicks by country — top 5
        Map<String, Long> clicksByCountry = events.stream()
                .filter(e -> e.getCountry() != null && !e.getCountry().equals("XX"))
                .collect(Collectors.groupingBy(AnalyticsEntity::getCountry, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        // Clicks by device
        Map<String, Long> clicksByDevice = events.stream()
                .filter(e -> e.getDevice() != null)
                .collect(Collectors.groupingBy(AnalyticsEntity::getDevice, Collectors.counting()));

        // Clicks over time (daily)
        Map<String, Long> dailyCounts = events.stream()
                .filter(e -> e.getShortCodeDate() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getShortCodeDate().split("#")[1], // Extract date from PK
                        Collectors.counting()));

        List<AnalyticsResponse.DailyClicks> clicksOverTime = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);
            clicksOverTime.add(new AnalyticsResponse.DailyClicks(
                    dateStr, dailyCounts.getOrDefault(dateStr, 0L)));
            current = current.plusDays(1);
        }

        // Build response
        AnalyticsResponse response = new AnalyticsResponse();
        response.setShortCode(shortCode);
        response.setTotalClicks(totalClicks);
        response.setUniqueClicks(uniqueClicks);
        response.setClicksByCountry(clicksByCountry);
        response.setClicksByDevice(clicksByDevice);
        response.setClicksOverTime(clicksOverTime);

        return response;
    }

    /**
     * Detects device type from User-Agent header.
     */
    private String detectDevice(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "desktop";
        if (BOT_PATTERN.matcher(userAgent).find()) return "bot";
        if (MOBILE_PATTERN.matcher(userAgent).find()) return "mobile";
        return "desktop";
    }

    /**
     * Extracts the real client IP, accounting for proxy headers.
     */
    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

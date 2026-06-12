package com.snaplink.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snaplink.model.AnalyticsEntity;
import com.snaplink.model.ClickEvent;
import com.snaplink.repository.AnalyticsRepository;
import com.snaplink.repository.LinkRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Consumes click events from the SQS queue and persists them
 * to the DynamoDB analytics table.
 *
 * <p>This consumer runs within the same Spring Boot application
 * for simplicity. In production, it could be deployed as a separate
 * Lambda function triggered by the SQS queue.
 */
@Component
public class ClickEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClickEventConsumer.class);

    private final AnalyticsRepository analyticsRepository;
    private final LinkRepository linkRepository;
    private final ObjectMapper objectMapper;

    public ClickEventConsumer(AnalyticsRepository analyticsRepository,
                              LinkRepository linkRepository,
                              ObjectMapper objectMapper) {
        this.analyticsRepository = analyticsRepository;
        this.linkRepository = linkRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Processes a click event message from SQS.
     *
     * <ol>
     *   <li>Deserializes the JSON payload into a {@link ClickEvent}</li>
     *   <li>Stores an {@link AnalyticsEntity} in DynamoDB</li>
     *   <li>Atomically increments the click count on the links table</li>
     * </ol>
     */
    @SqsListener("${aws.sqs.queue-name:snaplink-click-events}")
    public void handleClickEvent(String message) {
        try {
            ClickEvent event = objectMapper.readValue(message, ClickEvent.class);

            // Build analytics entity
            String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String pk = event.shortCode() + "#" + date;

            AnalyticsEntity entity = new AnalyticsEntity();
            entity.setShortCodeDate(pk);
            entity.setClickId(UUID.randomUUID().toString());
            entity.setTimestamp(event.timestamp());
            entity.setCountry(event.country());
            entity.setDevice(event.device());
            entity.setReferer(event.referer());
            entity.setIpHash(event.ipHash());

            // Save to analytics table
            analyticsRepository.save(entity);

            // Increment click count on links table
            linkRepository.incrementClickCount(event.shortCode());

            log.info("Processed click event: shortCode={}, device={}, country={}",
                    event.shortCode(), event.device(), event.country());

        } catch (Exception e) {
            log.error("Failed to process click event: {}", e.getMessage(), e);
            // Message will be retried by SQS visibility timeout, then sent to DLQ
            throw new RuntimeException("Click event processing failed", e);
        }
    }
}

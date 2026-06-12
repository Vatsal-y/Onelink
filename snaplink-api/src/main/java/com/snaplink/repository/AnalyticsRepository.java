package com.snaplink.repository;

import com.snaplink.model.AnalyticsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for CRUD operations on the {@code snaplink-analytics} DynamoDB table.
 */
@Repository
public class AnalyticsRepository {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsRepository.class);

    private final DynamoDbTable<AnalyticsEntity> table;

    public AnalyticsRepository(DynamoDbEnhancedClient enhancedClient,
                               @Value("${aws.dynamodb.tables.analytics:snaplink-analytics}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(AnalyticsEntity.class));
    }

    /**
     * Saves a single click analytics event.
     */
    public void save(AnalyticsEntity entity) {
        log.debug("Saving analytics: pk={}, sk={}", entity.getShortCodeDate(), entity.getClickId());
        table.putItem(entity);
    }

    /**
     * Retrieves all click events for a given short code within a date range.
     *
     * <p>Iterates over each day in the range and queries the partition key
     * {@code {shortCode}#{date}} for each day.
     *
     * @param shortCode the short code
     * @param startDate inclusive start date
     * @param endDate   inclusive end date
     * @return all analytics entities in the date range
     */
    public List<AnalyticsEntity> findByShortCodeAndDateRange(
            String shortCode, LocalDate startDate, LocalDate endDate) {

        List<AnalyticsEntity> results = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String pk = shortCode + "#" + current.format(DateTimeFormatter.ISO_LOCAL_DATE);

            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                    Key.builder().partitionValue(pk).build());

            table.query(r -> r.queryConditional(queryConditional))
                    .stream()
                    .flatMap(page -> page.items().stream())
                    .forEach(results::add);

            current = current.plusDays(1);
        }

        return results;
    }
}

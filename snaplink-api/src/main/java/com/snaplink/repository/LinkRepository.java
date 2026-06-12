package com.snaplink.repository;

import com.snaplink.model.LinkEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.List;
import java.util.Map;

/**
 * Repository for CRUD operations on the {@code snaplink-links} DynamoDB table.
 */
@Repository
public class LinkRepository {

    private static final Logger log = LoggerFactory.getLogger(LinkRepository.class);

    private final DynamoDbTable<LinkEntity> table;
    private final DynamoDbIndex<LinkEntity> userIdIndex;
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public LinkRepository(DynamoDbEnhancedClient enhancedClient,
                          DynamoDbClient dynamoDbClient,
                          @Value("${aws.dynamodb.tables.links:snaplink-links}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(LinkEntity.class));
        this.userIdIndex = table.index("userId-createdAt-index");
    }

    /**
     * Saves a new link entity. Overwrites if the same shortCode already exists.
     */
    public void save(LinkEntity entity) {
        log.debug("Saving link: shortCode={}", entity.getShortCode());
        table.putItem(entity);
    }

    /**
     * Finds a link by its short code (partition key).
     *
     * @return the link entity, or {@code null} if not found
     */
    public LinkEntity findByShortCode(String shortCode) {
        return table.getItem(Key.builder().partitionValue(shortCode).build());
    }

    /**
     * Checks whether a short code already exists.
     */
    public boolean existsByShortCode(String shortCode) {
        return findByShortCode(shortCode) != null;
    }

    /**
     * Lists all links owned by a given user, ordered by creation date (descending).
     */
    public List<LinkEntity> findByUserId(String userId) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build());

        return userIdIndex.query(r -> r
                        .queryConditional(queryConditional)
                        .scanIndexForward(false)) // newest first
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    /**
     * Deletes a link by short code.
     */
    public void delete(String shortCode) {
        log.debug("Deleting link: shortCode={}", shortCode);
        table.deleteItem(Key.builder().partitionValue(shortCode).build());
    }

    /**
     * Atomically increments the click count for a link.
     * Uses a low-level UpdateItem to perform {@code ADD clickCount :1}.
     */
    public void incrementClickCount(String shortCode) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("shortCode", AttributeValue.builder().s(shortCode).build()))
                .attributeUpdates(Map.of(
                        "clickCount", AttributeValueUpdate.builder()
                                .value(AttributeValue.builder().n("1").build())
                                .action(AttributeAction.ADD)
                                .build()
                ))
                .build();

        dynamoDbClient.updateItem(request);
    }
}

#!/bin/bash
# ======================================================
# LocalStack init script — runs when LocalStack is ready
# Creates DynamoDB tables, SQS queue, and S3 bucket
# ======================================================

echo "=== Initializing SnapLink local resources ==="

# ---- DynamoDB Tables (via LocalStack is not used for DynamoDB — using DynamoDB Local) ----
# These commands target the separate DynamoDB Local container

# Create links table
aws dynamodb create-table \
  --endpoint-url http://dynamodb-local:8000 \
  --table-name snaplink-links \
  --attribute-definitions \
    AttributeName=shortCode,AttributeType=S \
    AttributeName=userId,AttributeType=S \
    AttributeName=createdAt,AttributeType=S \
  --key-schema \
    AttributeName=shortCode,KeyType=HASH \
  --global-secondary-indexes \
    'IndexName=userId-createdAt-index,KeySchema=[{AttributeName=userId,KeyType=HASH},{AttributeName=createdAt,KeyType=RANGE}],Projection={ProjectionType=ALL}' \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1 \
  2>/dev/null || echo "Links table already exists"

# Enable TTL on links table
aws dynamodb update-time-to-live \
  --endpoint-url http://dynamodb-local:8000 \
  --table-name snaplink-links \
  --time-to-live-specification "Enabled=true,AttributeName=expiresAt" \
  --region us-east-1 \
  2>/dev/null || echo "TTL already enabled"

# Create analytics table
aws dynamodb create-table \
  --endpoint-url http://dynamodb-local:8000 \
  --table-name snaplink-analytics \
  --attribute-definitions \
    AttributeName=shortCodeDate,AttributeType=S \
    AttributeName=clickId,AttributeType=S \
  --key-schema \
    AttributeName=shortCodeDate,KeyType=HASH \
    AttributeName=clickId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1 \
  2>/dev/null || echo "Analytics table already exists"

# ---- SQS Queue ----
awslocal sqs create-queue \
  --queue-name snaplink-click-events \
  2>/dev/null || echo "SQS queue already exists"

# Create Dead Letter Queue
awslocal sqs create-queue \
  --queue-name snaplink-click-events-dlq \
  2>/dev/null || echo "SQS DLQ already exists"

# ---- S3 Bucket ----
awslocal s3 mb s3://snaplink-qrcodes \
  2>/dev/null || echo "S3 bucket already exists"

echo "=== SnapLink local resources initialized ==="

# SnapLink — Serverless URL Shortener

> A fully serverless, production-grade URL shortening service built on **Spring Boot 3** and **AWS**.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=flat-square)
![AWS](https://img.shields.io/badge/AWS-Lambda%20%7C%20DynamoDB%20%7C%20SQS-FF9900?style=flat-square)
![React](https://img.shields.io/badge/React-Vite-61DAFB?style=flat-square)

---

## Architecture

```mermaid
graph TD
    classDef client fill:#3b82f6,stroke:#1d4ed8,stroke-width:2px,color:#fff;
    classDef gate fill:#eab308,stroke:#ca8a04,stroke-width:2px,color:#000;
    classDef compute fill:#10b981,stroke:#047857,stroke-width:2px,color:#fff;
    classDef store fill:#6366f1,stroke:#4f46e5,stroke-width:2px,color:#fff;
    classDef queue fill:#f97316,stroke:#ea580c,stroke-width:2px,color:#fff;

    subgraph Client ["Clients & Entry Points"]
        Dash["React SPA (Vite Dashboard)"]:::client
        Clicker["Link User (Browser click/app)"]:::client
    end

    subgraph Gateway ["API Routing & Security"]
        APIGW["AWS API Gateway"]:::gate
        CognitoAuth["AWS Cognito (JWT Verification)"]:::gate
    end

    subgraph SpringLambda ["AWS Lambda Runtime (Spring Boot)"]
        Handler["Spring Boot Web Handler (SnapStart)"]:::compute
        Consumer["ClickEvent Consumer (SQS Listener)"]:::compute
    end

    subgraph Storage ["Databases & Object Storage"]
        Redis["ElastiCache Redis (Cache-Aside: Redirects)"]:::store
        DynamoLinks["DynamoDB (links table)"]:::store
        DynamoAnalytics["DynamoDB (analytics table)"]:::store
        S3["Amazon S3 (QR code bucket)"]:::store
    end

    subgraph Queuing ["Message Pipeline"]
        SQS["AWS SQS (click-events queue)"]:::queue
    end

    %% Auth Flow
    Dash -->|1. Authenticate / Register| APIGW
    APIGW -.->|Verify JWT| CognitoAuth
    CognitoAuth -.->|Token valid| APIGW

    %% Dashboard Flow
    Dash -->|2. Create / Manage Link / Fetch Analytics| APIGW
    APIGW -->|Proxy HTTP Request| Handler
    Handler -->|Create QR Code| S3
    Handler -->|Write Link metadata| DynamoLinks
    Handler -->|Read Daily Analytics| DynamoAnalytics

    %% Redirect Flow
    Clicker -->|3. GET /{shortCode}| APIGW
    APIGW -->|Proxy HTTP Request| Handler
    Handler -->|3a. Read cache| Redis
    Redis -.->|Cache HIT: Return long URL| Handler
    Redis -.->|Cache MISS| Handler
    Handler -->|3b. Query Link| DynamoLinks
    Handler -->|3c. Update cache| Redis

    %% Async Analytics Flow
    Handler -->|4. Push click event (async)| SQS
    SQS -->|5. SQS Poller message trigger| Consumer
    Consumer -->|6. Write click details| DynamoAnalytics
```

## Features

| Feature | Description |
|---------|-------------|
| **URL Shortening** | 6-char base-62 codes with collision detection |
| **Custom Aliases** | Vanity URLs (3–30 chars) with conflict suggestions |
| **Link Expiry** | 1h / 1d / 7d / 30d / never — via DynamoDB TTL |
| **Click Analytics** | Geo, device, timestamp — async via SQS pipeline |
| **Redis Caching** | Cache-aside pattern, sub-10ms redirects |
| **QR Codes** | 512×512 PNG via ZXing, stored in S3 |
| **JWT Auth** | AWS Cognito with Spring Security |
| **Analytics Dashboard** | React + Vite with Chart.js visualizations |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3, Java 21, Maven |
| Runtime | AWS Lambda (SnapStart, ARM64) |
| API | AWS API Gateway |
| Database | AWS DynamoDB (on-demand) |
| Cache | AWS ElastiCache (Redis) |
| Queue | AWS SQS + Dead Letter Queue |
| Auth | AWS Cognito |
| Storage | AWS S3 |
| Frontend | React 19 + Vite |
| QR | ZXing 3.5 |

## Project Structure

```
snaplink-api/          # Spring Boot backend
├── src/main/java/com/snaplink/
│   ├── controller/    # REST endpoints
│   ├── service/       # Business logic
│   ├── repository/    # DynamoDB operations
│   ├── model/         # Entities + DTOs
│   ├── config/        # AWS + Security config
│   ├── consumer/      # SQS click event consumer
│   ├── handler/       # Lambda entry point
│   ├── exception/     # Error handling
│   └── util/          # Base62, UrlValidator, IpHasher

snaplink-dashboard/    # React frontend
├── src/
│   ├── pages/         # Login, Register, Dashboard, Create, Analytics
│   ├── components/    # Navbar, Charts, QR Modal
│   ├── api/           # API client with JWT
│   └── context/       # Auth context

infrastructure/        # AWS SAM template
```

## Local Development

### Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 18+
- Docker (for DynamoDB Local, Redis, LocalStack)

### 1. Start Infrastructure
```bash
docker-compose up -d
```

### 2. Run Backend
```bash
cd snaplink-api
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Run Frontend
```bash
cd snaplink-dashboard
npm install
npm run dev
```

### 4. Test API
```bash
# Create short link
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://github.com", "expiresIn": "7d"}'

# Redirect
curl -L http://localhost:8080/{shortCode}

# Analytics
curl http://localhost:8080/api/analytics/{shortCode}
```

## AWS Deployment

```bash
cd infrastructure
sam build
sam deploy --guided
```

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Register user | No |
| POST | `/auth/login` | Login → JWT | No |
| POST | `/api/shorten` | Create short link | Yes |
| GET | `/{code}` | Redirect (301) | No |
| GET | `/api/links` | List user's links | Yes |
| DELETE | `/api/links/{code}` | Delete link | Yes |
| GET | `/api/analytics/{code}` | Click analytics | Yes |
| GET | `/api/links/{code}/qr` | QR code URL | Yes |

## Resume Bullet Points

- Built serverless URL Shortener (SnapLink) using Spring Boot on AWS Lambda + API Gateway
- Stored URL mappings with custom aliases and auto-expiry using DynamoDB native TTL
- Designed async click-analytics pipeline with AWS SQS + Lambda tracking geo, device, and timestamp
- Implemented Redis cache-aside pattern (ElastiCache) reducing DynamoDB reads by 80%+
- Auto-generated QR codes per link using ZXing, stored in S3, served via pre-signed URLs
- Secured all endpoints with JWT authentication via AWS Cognito
- Built React + Vite analytics dashboard with Chart.js visualizations

## License

MIT

# PMD Proposal Client Consumer

A Kafka-based event consumer service built with Spring Boot that processes proposal client requests using Hexagonal Architecture.

## 🚀 Quick Start

### Prerequisites

- Java 21
- Maven 3.6+
- Kafka (running locally or accessible endpoint)

### Build

```bash
./mvnw clean install
```

### Run

```bash
./mvnw spring-boot:run
```

## 📋 Configuration

Configure Kafka connection in `src/main/resources/application.yaml`:

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: pmd-proposal-client-consumer
      topic: proposal-client-topic
```

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports and Adapters):

```
├── domain/          # Business logic (framework-independent)
├── application/     # Use cases and ports (orchestration)
└── infrastructure/  # Technical implementation (Kafka, DTOs)
```

## 📚 Documentation

- **[ABOUT.md](ABOUT.md)** - Project overview and architecture layers

## 🔄 Data Flow

1. Kafka message received → **Input Adapter**
2. DTO mapped to Domain model → **Mapper**
3. Business validation applied → **Domain Model**
4. Use case executed → **Application Service**
5. Event published → **Output Adapter**

## 🧪 Testing

```bash
./mvnw test
```

## 📦 Technologies

- Java 21
- Spring Boot 4.0.2
- Spring Kafka
- Maven
- Lombok


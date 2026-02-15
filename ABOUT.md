# About PMD Proposal Client Consumer

## Overview

This application is a Kafka-based event consumer service that processes incoming proposal client requests. It receives proposal data from external systems via Kafka, validates business rules, enriches the data with domain logic, and publishes processed events for downstream consumption.

The service acts as a bridge between external proposal intake systems and internal processing workflows, ensuring data quality and business rule compliance before proposals enter the core business system.

---

## Architecture

This project implements **Hexagonal Architecture** (Ports and Adapters pattern) to maintain clean separation between business logic and technical infrastructure.

```
src/main/java/com/pmdpcc/
│
├── domain/                          # Business Logic Core
│   ├── model/
│   ├── service/
│   └── exception/
│
├── application/                     # Use Cases & Orchestration
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   ├── service/
│   └── config/
│
└── infrastructure/                  # Technical Implementation
    └── adapter/
        ├── in/
        │   └── kafka/
        │       ├── dto/
        │       ├── mapper/
        │       └── config/
        └── out/
            └── kafka/
                ├── dto/
                └── mapper/
```

---

## Layer Responsibilities

### 1. Domain Layer (`domain/`)

**Purpose:** Contains pure business logic with zero framework dependencies.

**Components:**
- **`model/`**: Business entities with validation and business rules
  - Immutable POJOs representing core business concepts
  - Self-validating with business rule enforcement
  - Contains domain behavior (not just data)

- **`service/`**: Domain services for multi-entity business logic
  - Stateless services for complex business rules
  - Logic that doesn't naturally belong to a single entity
  - Pure business calculations and decisions

- **`exception/`**: Domain-specific exceptions
  - Business rule violations
  - Validation failures

**Key Characteristics:**
- No Spring annotations
- No external dependencies
- Framework-agnostic
- Testable without infrastructure

---

### 2. Application Layer (`application/`)

**Purpose:** Orchestrates business use cases and defines contracts (ports) for external interactions.

**Components:**
- **`port/in/`**: Input Ports (Driving Ports)
  - Interfaces defining what the application **can do**
  - Represent use cases
  - Called by driving adapters
  - Use domain models in signatures

- **`port/out/`**: Output Ports (Driven Ports)
  - Interfaces defining what the application **needs**
  - Represent external dependencies
  - Implemented by driven adapters
  - Use domain models in signatures

- **`service/`**: Application Services
  - Implement input ports
  - Orchestrate domain logic
  - Coordinate workflow between domain and ports
  - Manage transaction boundaries
  - Handle logging and monitoring

- **`config/`**: Application Configuration
  - Wire domain services as Spring beans
  - Configure application-level dependencies

**Key Characteristics:**
- Depends only on domain layer
- No infrastructure code
- Thin orchestration layer
- Business logic delegated to domain

---

### 3. Infrastructure Layer (`infrastructure/`)

**Purpose:** Implements technical concerns and integrations with external systems.

**Components:**

#### **`adapter/in/`** (Driving Adapters)
- Receive input from external world
- **Examples:** Kafka consumers, REST controllers, scheduled jobs

**Structure:**
- **`kafka/`**: Kafka consumer implementation
  - **`dto/`**: Data Transfer Objects for deserialization
  - **`mapper/`**: Converts DTOs → Domain models
  - **`config/`**: Kafka consumer configuration

**Responsibilities:**
1. Receive messages from Kafka
2. Map infrastructure DTOs to domain models
3. Call input ports with domain models
4. Handle technical errors

#### **`adapter/out/`** (Driven Adapters)
- Implement interactions with external systems
- **Examples:** Kafka publishers, database repositories, HTTP clients

**Structure:**
- **`kafka/`**: Kafka publisher implementation
  - **`dto/`**: Data Transfer Objects for serialization
  - **`mapper/`**: Converts Domain models → DTOs

**Responsibilities:**
1. Implement output ports
2. Map domain models to infrastructure DTOs
3. Interact with external systems (Kafka, databases, APIs)
4. Handle connection and serialization details

**Key Characteristics:**
- Contains all framework-specific code
- Depends on application and domain layers
- Can be replaced without touching business logic
- Isolates technical complexity

---

## Data Flow

```
External System (Kafka)
        ↓
[Input Adapter] receives ProposalClientEvent (DTO)
        ↓
[Mapper] converts DTO → ProposalClient (Domain)
        ↓
[Input Port] PublishProposalClientPort.publish()
        ↓
[Application Service] orchestrates workflow
        ├→ [Domain Model] validate(), business rules
        ├→ [Domain Service] complex multi-entity logic
        └→ [Output Port] ProposalClientEventPublisher.publish()
                ↓
        [Output Adapter] receives ProposalClient (Domain)
                ↓
        [Mapper] converts Domain → ProposalEvent (DTO)
                ↓
        External System (Kafka)
```

---

## Benefits of This Architecture

### ✅ **Testability**
- Domain logic testable without Spring or Kafka
- Application services testable with mocked ports
- Adapters testable independently

### ✅ **Maintainability**
- Clear separation of concerns
- Business logic isolated from infrastructure
- Changes to infrastructure don't affect domain

### ✅ **Flexibility**
- Easy to swap Kafka for RabbitMQ or other messaging systems
- Can add REST endpoints alongside Kafka consumers
- Multiple adapters can share the same ports

### ✅ **Technology Independence**
- Domain has no framework coupling
- Can migrate frameworks without rewriting business logic
- Infrastructure changes isolated to adapter layer

---

## Key Design Principles

- **Dependency Inversion**: Application depends on abstractions (ports), not implementations
- **Single Responsibility**: Each layer has one clear purpose
- **Open/Closed**: Open for extension (add new adapters), closed for modification (business logic unchanged)
- **Domain-Driven Design**: Rich domain models with behavior, not anemic data structures

---

## Technologies Used

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Kafka**
- **Lombok** (infrastructure layer only)
- **Maven**

---
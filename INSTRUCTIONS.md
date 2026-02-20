# Hexagonal Architecture (Ports and Adapters) Implementation Guide

This document provides detailed instructions for replicating the ports and adapters architecture pattern used in this project.

## Table of Contents
- [Directory Structure](#directory-structure)
- [Layer Implementation Rules](#layer-implementation-rules)
  - [1. Domain Layer](#1-domain-layer)
  - [2. Application Layer](#2-application-layer)
  - [3. Infrastructure Layer](#3-infrastructure-layer)
- [Key Principles](#key-principles)
- [Maven Dependencies](#maven-dependencies)
- [Configuration](#configuration)

---

## Directory Structure

```
src/main/java/{base-package}/
├── domain/                    # Pure business logic, ZERO framework dependencies
│   ├── model/                # Rich domain entities with business behavior
│   ├── service/              # Domain services for multi-entity logic
│   └── exception/            # Business rule exceptions
├── application/              # Use case orchestration
│   ├── port/
│   │   ├── in/              # Input ports (interfaces defining what app CAN DO)
│   │   └── out/             # Output ports (interfaces defining what app NEEDS)
│   ├── service/             # Application services implementing input ports
│   └── config/              # Wire domain services as Spring beans
└── infrastructure/           # Framework-specific code
    └── adapter/
        ├── in/              # Driving adapters (receive external input)
        │   └── {technology}/  # e.g., kafka, rest, scheduled
        │       ├── dto/
        │       ├── mapper/
        │       └── config/
        └── out/             # Driven adapters (call external systems)
            └── {technology}/  # e.g., kafka, database, httpclient
                ├── dto/
                └── mapper/
```

---

## Layer Implementation Rules

### 1. Domain Layer

The domain layer is the **heart of your application** and contains all business logic.

#### Critical Rules:
- ❌ **NO** Spring annotations (`@Service`, `@Component`, `@Autowired`, etc.)
- ❌ **NO** framework imports (Spring, Kafka, Jackson, etc.)
- ✅ Only pure Java and domain exceptions
- ✅ Immutable objects (final fields, no setters)
- ✅ Constructor-based instantiation
- ✅ Rich behavior, not anemic data structures

#### Domain Model (`domain/model/`)

Rich domain objects with validation and business logic.

```java
package com.yourpackage.domain.model;

import com.yourpackage.domain.exception.InvalidEntityException;

/**
 * YourEntity - Domain Model (Rich Domain Object)
 *
 * This is where BUSINESS LOGIC lives:
 * - Validation rules
 * - Business calculations
 * - State transitions
 * - Invariant enforcement
 *
 * Domain models are:
 * - Pure POJOs (no framework annotations)
 * - Immutable (final fields, no setters)
 * - Self-validating
 * - Framework-independent
 */
public class YourEntity {
    private final String id;
    private final String name;
    private final ValueObject valueObject;

    // Constructor with all fields
    public YourEntity(String id, String name, ValueObject valueObject) {
        this.id = id;
        this.name = name;
        this.valueObject = valueObject;
    }

    // ========================================
    // BUSINESS LOGIC - Validation
    // ========================================

    /**
     * Validates business rules.
     * Called by application service before processing.
     */
    public void validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidEntityException("ID is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEntityException("Name is required");
        }
        if (valueObject == null) {
            throw new InvalidEntityException("ValueObject is required");
        }
        valueObject.validate();
    }

    // ========================================
    // BUSINESS LOGIC - Domain Behavior
    // ========================================

    /**
     * Business rule: Check if entity meets special criteria
     */
    public boolean isSpecialCase() {
        return name.length() > 10
            && valueObject.getAmount() > 1000;
    }

    /**
     * Business rule: Calculate priority score
     */
    public int calculatePriorityScore() {
        int score = 0;

        if (valueObject.getAmount() > 5000) {
            score += 50;
        } else if (valueObject.getAmount() > 1000) {
            score += 30;
        } else {
            score += 10;
        }

        if (name.startsWith("VIP")) {
            score += 20;
        }

        return score;
    }

    /**
     * Business rule: Get category for reporting
     */
    public String getCategory() {
        if (valueObject.getAmount() > 10000) {
            return "PREMIUM";
        } else if (valueObject.getAmount() > 1000) {
            return "STANDARD";
        }
        return "BASIC";
    }

    /**
     * Business rule: Check if entity is complete
     */
    public boolean isComplete() {
        return id != null && !id.trim().isEmpty()
            && name != null && !name.trim().isEmpty()
            && valueObject != null && valueObject.isValid();
    }

    // Only getters, NO setters (immutability)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ValueObject getValueObject() {
        return valueObject;
    }

    // ========================================
    // Value Object - Nested class
    // ========================================

    /**
     * ValueObject - Immutable value object with its own business logic
     * Value objects define equality by value, not identity
     */
    public static class ValueObject {
        private final Integer amount;
        private final String currency;

        public ValueObject(Integer amount, String currency) {
            this.amount = amount;
            this.currency = currency;
        }

        // Business validation
        public void validate() {
            if (amount != null && amount < 0) {
                throw new InvalidEntityException("Amount cannot be negative");
            }
        }

        // Business logic - Check if valid
        public boolean isValid() {
            return amount != null && amount >= 0;
        }

        // Business logic - Format for display
        public String formatForDisplay() {
            if (amount == null) {
                return "Amount not specified";
            }
            return String.format("%s %,d", currency != null ? currency : "USD", amount);
        }

        public Integer getAmount() {
            return amount;
        }

        public String getCurrency() {
            return currency;
        }
    }
}
```

#### Domain Service (`domain/service/`)

Domain services are used when business logic involves **multiple domain objects** or doesn't naturally belong to a single entity.

```java
package com.yourpackage.domain.service;

import com.yourpackage.domain.model.YourEntity;

/**
 * YourDomainService - Domain Service
 *
 * Domain services are used when:
 * - Business logic involves MULTIPLE domain objects
 * - Logic doesn't naturally belong to a single entity
 * - You need to coordinate between different domain models
 *
 * Domain services:
 * - Live in domain layer
 * - Are stateless
 * - Contain pure business logic
 * - Have NO infrastructure dependencies
 * - Work only with domain models
 */
public class YourDomainService {

    /**
     * Business rule: Check if entity should be auto-approved
     * Complex rule involving multiple factors
     */
    public boolean shouldAutoApprove(YourEntity entity) {
        return entity.isComplete()
            && entity.getValueObject().getAmount() != null
            && entity.getValueObject().getAmount() <= 1000
            && !entity.isSpecialCase();
    }

    /**
     * Business rule: Determine review team based on entity characteristics
     */
    public String assignReviewTeam(YourEntity entity) {
        if (entity.isSpecialCase()) {
            return "SENIOR_TEAM";
        }

        String category = entity.getCategory();
        switch (category) {
            case "PREMIUM":
                return "PREMIUM_TEAM";
            case "STANDARD":
                return "STANDARD_TEAM";
            case "BASIC":
            default:
                return "BASIC_TEAM";
        }
    }

    /**
     * Business rule: Calculate estimated processing time in hours
     */
    public int calculateEstimatedProcessingTime(YourEntity entity) {
        int baseTime = 24; // Default 24 hours

        if (entity.isSpecialCase()) {
            baseTime = 4; // Special cases: 4 hours
        } else if ("PREMIUM".equals(entity.getCategory())) {
            baseTime = 8; // Premium: 8 hours
        }

        return baseTime;
    }
}
```

#### Domain Exception (`domain/exception/`)

```java
package com.yourpackage.domain.exception;

public class InvalidEntityException extends RuntimeException {
    public InvalidEntityException(String message) {
        super(message);
    }
}
```

---

### 2. Application Layer

The application layer **orchestrates use cases** and defines **contracts (ports)** for external interactions.

#### Input Port (`application/port/in/`)

Input ports define **what the application CAN DO** (use cases).

```java
package com.yourpackage.application.port.in;

import com.yourpackage.domain.model.YourEntity;

/**
 * Input Port - Defines use case interface
 *
 * Input ports:
 * - Define what the application can do
 * - Use domain models in method signatures
 * - Are implemented by application services
 * - Are called by driving adapters (input adapters)
 */
public interface ProcessEntityPort {
    void process(YourEntity entity);
}
```

#### Output Port (`application/port/out/`)

Output ports define **what the application NEEDS** from external systems.

```java
package com.yourpackage.application.port.out;

import com.yourpackage.domain.model.YourEntity;

/**
 * Output Port - Defines dependency interface
 *
 * Output ports:
 * - Define what the application needs from external systems
 * - Use domain models in method signatures
 * - Are implemented by driven adapters (output adapters)
 * - Are called by application services
 */
public interface EntityRepository {
    void save(YourEntity entity);
    YourEntity findById(String id);
}

public interface EntityEventPublisher {
    void publish(YourEntity entity);
}
```

#### Application Service (`application/service/`)

Application services implement input ports and orchestrate the use case workflow.

```java
package com.yourpackage.application.service;

import com.yourpackage.application.port.in.ProcessEntityPort;
import com.yourpackage.application.port.out.EntityRepository;
import com.yourpackage.application.port.out.EntityEventPublisher;
import com.yourpackage.domain.model.YourEntity;
import com.yourpackage.domain.service.YourDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application Service - Orchestrates use case
 *
 * Application services:
 * - Implement input ports
 * - Orchestrate workflow between domain and output ports
 * - Delegate business logic to domain layer
 * - Manage transaction boundaries
 * - Handle logging and monitoring
 * - NO business logic here - delegate to domain!
 */
@Service
@Slf4j
@RequiredArgsConstructor  // Constructor injection only
public class ProcessEntityService implements ProcessEntityPort {

    // Inject output ports
    private final EntityRepository entityRepository;
    private final EntityEventPublisher entityEventPublisher;

    // Inject domain services
    private final YourDomainService domainService;

    @Override
    public void process(YourEntity entity) {
        log.info("Processing entity: {}", entity.getId());

        // Step 1: Validate domain rules
        entity.validate();
        log.info("Entity validated successfully");

        // Step 2: Execute domain logic (delegation)
        boolean shouldAutoApprove = domainService.shouldAutoApprove(entity);
        String assignedTeam = domainService.assignReviewTeam(entity);

        log.info("Entity auto-approve: {}, assigned to: {}",
            shouldAutoApprove, assignedTeam);

        // Step 3: Persist via output port
        entityRepository.save(entity);
        log.info("Entity saved to repository");

        // Step 4: Publish event via output port
        entityEventPublisher.publish(entity);
        log.info("Entity event published successfully");
    }
}
```

#### Domain Wiring Config (`application/config/`)

Wire domain services as Spring beans here, **not in the domain layer**.

```java
package com.yourpackage.application.config;

import com.yourpackage.domain.service.YourDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain Wiring Configuration
 *
 * Purpose:
 * - Wire domain services as Spring beans
 * - Keep domain layer framework-agnostic
 * - Domain services have NO Spring annotations
 */
@Configuration
public class DomainWiringConfig {

    @Bean
    public YourDomainService yourDomainService() {
        return new YourDomainService();
    }
}
```

---

### 3. Infrastructure Layer

The infrastructure layer contains **all framework-specific code** and implements adapters.

#### Input Adapter - Kafka Consumer

**Inbound Adapter Structure:**
```
infrastructure/adapter/in/kafka/
├── dto/              # Data Transfer Objects for deserialization
├── mapper/           # Convert DTO → Domain
├── config/           # Kafka consumer configuration
└── YourConsumer.java # Kafka listener implementation
```

**DTO (`infrastructure/adapter/in/kafka/dto/`):**

```java
package com.yourpackage.infrastructure.adapter.in.kafka.dto;

import lombok.Data;

/**
 * DTO for Kafka deserialization
 *
 * DTOs:
 * - Are framework-specific (can use Lombok, Jackson annotations)
 * - Are mutable (setters allowed)
 * - Are used only at adapter boundaries
 * - Are NEVER passed to domain layer
 */
@Data
public class EntityIncomingEvent {
    private String id;
    private String name;
    private ValueObjectDto valueObject;

    @Data
    public static class ValueObjectDto {
        private Integer amount;
        private String currency;
    }
}
```

**Mapper (`infrastructure/adapter/in/kafka/mapper/`):**

```java
package com.yourpackage.infrastructure.adapter.in.kafka.mapper;

import com.yourpackage.domain.model.YourEntity;
import com.yourpackage.infrastructure.adapter.in.kafka.dto.EntityIncomingEvent;
import org.springframework.stereotype.Component;

/**
 * Mapper - Converts DTO to Domain Model
 *
 * Mappers:
 * - Live in infrastructure layer
 * - Convert between DTOs and domain models
 * - Are stateless
 * - Handle null checks
 */
@Component
public class EntityIncomingEventMapper {

    public YourEntity toDomain(EntityIncomingEvent event) {
        if (event == null) {
            return null;
        }

        // Map nested value objects
        YourEntity.ValueObject valueObject = null;
        if (event.getValueObject() != null) {
            valueObject = new YourEntity.ValueObject(
                event.getValueObject().getAmount(),
                event.getValueObject().getCurrency()
            );
        }

        // Create immutable domain model
        return new YourEntity(
            event.getId(),
            event.getName(),
            valueObject
        );
    }
}
```

**Consumer (`infrastructure/adapter/in/kafka/`):**

```java
package com.yourpackage.infrastructure.adapter.in.kafka;

import com.yourpackage.application.port.in.ProcessEntityPort;
import com.yourpackage.domain.model.YourEntity;
import com.yourpackage.infrastructure.adapter.in.kafka.dto.EntityIncomingEvent;
import com.yourpackage.infrastructure.adapter.in.kafka.mapper.EntityIncomingEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer - Input Adapter (Driving Adapter)
 *
 * Input adapters:
 * - Receive input from external systems
 * - Convert DTOs to domain models
 * - Call input ports with domain models
 * - Handle technical errors
 * - Should NOT contain business logic
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntityConsumer {

    // Inject input port (use case interface)
    private final ProcessEntityPort processEntityPort;

    // Inject mapper
    private final EntityIncomingEventMapper mapper;

    @KafkaListener(
        topics = "${spring.kafka.consumer.topic}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(EntityIncomingEvent event) {
        log.info("Received Kafka message: {}", event.getId());

        try {
            // 1. Convert DTO to domain model
            YourEntity domainModel = mapper.toDomain(event);

            // 2. Call input port (use case)
            processEntityPort.process(domainModel);

            log.info("Successfully processed message: {}", event.getId());
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", event.getId(), e);
            // Handle error (dead letter queue, retry, etc.)
        }
    }
}
```

**Kafka Config (`infrastructure/adapter/in/kafka/config/`):**

```java
package com.yourpackage.infrastructure.adapter.in.kafka.config;

import com.yourpackage.infrastructure.adapter.in.kafka.dto.EntityIncomingEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Error handling and Jackson deserializer
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);

        // Jackson configuration
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.yourpackage.*");
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, EntityIncomingEvent.class.getName());
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return props;
    }

    @Bean
    public ConsumerFactory<String, EntityIncomingEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EntityIncomingEvent>
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EntityIncomingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

#### Output Adapter - Kafka Publisher

**Outbound Adapter Structure:**
```
infrastructure/adapter/out/kafka/
├── dto/                      # Data Transfer Objects for serialization
├── mapper/                   # Convert Domain → DTO
└── KafkaEntityPublisher.java # Publisher implementation
```

**DTO (`infrastructure/adapter/out/kafka/dto/`):**

```java
package com.yourpackage.infrastructure.adapter.out.kafka.dto;

import lombok.Data;

@Data
public class EntityOutgoingEvent {
    private String id;
    private String name;
    private ValueObjectDto valueObject;

    @Data
    public static class ValueObjectDto {
        private Integer amount;
        private String currency;
    }
}
```

**Mapper (`infrastructure/adapter/out/kafka/mapper/`):**

```java
package com.yourpackage.infrastructure.adapter.out.kafka.mapper;

import com.yourpackage.domain.model.YourEntity;
import com.yourpackage.infrastructure.adapter.out.kafka.dto.EntityOutgoingEvent;
import org.springframework.stereotype.Component;

/**
 * Mapper - Converts Domain Model to DTO
 */
@Component
public class EntityOutgoingEventMapper {

    public EntityOutgoingEvent toDTO(YourEntity domain) {
        if (domain == null) {
            return null;
        }

        EntityOutgoingEvent event = new EntityOutgoingEvent();
        event.setId(domain.getId());
        event.setName(domain.getName());

        if (domain.getValueObject() != null) {
            EntityOutgoingEvent.ValueObjectDto dto = new EntityOutgoingEvent.ValueObjectDto();
            dto.setAmount(domain.getValueObject().getAmount());
            dto.setCurrency(domain.getValueObject().getCurrency());
            event.setValueObject(dto);
        }

        return event;
    }
}
```

**Publisher (`infrastructure/adapter/out/kafka/`):**

```java
package com.yourpackage.infrastructure.adapter.out.kafka;

import com.yourpackage.application.port.out.EntityEventPublisher;
import com.yourpackage.domain.model.YourEntity;
import com.yourpackage.infrastructure.adapter.out.kafka.dto.EntityOutgoingEvent;
import com.yourpackage.infrastructure.adapter.out.kafka.mapper.EntityOutgoingEventMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka Publisher - Output Adapter (Driven Adapter)
 *
 * Output adapters:
 * - Implement output ports
 * - Convert domain models to DTOs
 * - Interact with external systems
 * - Handle connection and serialization details
 */
@Component
@AllArgsConstructor
@Slf4j
public class KafkaEntityPublisher implements EntityEventPublisher {

    private final KafkaTemplate<String, EntityOutgoingEvent> kafkaTemplate;
    private final EntityOutgoingEventMapper mapper;

    @Override
    public void publish(YourEntity entity) {
        log.info("Publishing entity to Kafka: {}", entity.getId());

        // Convert domain to DTO
        EntityOutgoingEvent event = mapper.toDTO(entity);

        // Publish to Kafka
        kafkaTemplate.send("output-topic", entity.getId(), event);

        log.info("Entity published successfully: {}", entity.getId());
    }
}
```

#### Output Adapter - Database Repository (Example)

```java
package com.yourpackage.infrastructure.adapter.out.database;

import com.yourpackage.application.port.out.EntityRepository;
import com.yourpackage.domain.model.YourEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaEntityRepository implements EntityRepository {

    private final EntityJpaRepository jpaRepository; // Spring Data JPA
    private final EntityJpaMapper mapper;

    @Override
    public void save(YourEntity entity) {
        log.info("Saving entity to database: {}", entity.getId());
        EntityJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        jpaRepository.save(jpaEntity);
        log.info("Entity saved successfully");
    }

    @Override
    public YourEntity findById(String id) {
        log.info("Finding entity by id: {}", id);
        EntityJpaEntity jpaEntity = jpaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(id));
        return mapper.toDomain(jpaEntity);
    }
}
```

---

## Key Principles

### Dependency Rule
**Dependencies flow inward only:**
```
Infrastructure → Application → Domain
```
- Infrastructure depends on Application and Domain
- Application depends on Domain
- **Domain depends on NOTHING** (pure Java)

### Separation of Concerns

| Layer | Responsibility | Contains |
|-------|---------------|----------|
| **Domain** | Business logic | Models, services, exceptions |
| **Application** | Use case orchestration | Ports, services, config |
| **Infrastructure** | Technical implementation | Adapters, DTOs, mappers, config |

### Port Naming Conventions

| Type | Naming | Example |
|------|--------|---------|
| Input Port | `{UseCase}Port` | `ProcessEntityPort` |
| Output Port | `{Dependency}{Type}` | `EntityRepository`, `EntityEventPublisher` |

### Adapter Naming Conventions

| Type | Naming | Example |
|------|--------|---------|
| Input Adapter | `{Technology}{Entity}Consumer` | `KafkaEntityConsumer`, `RestEntityController` |
| Output Adapter | `{Technology}{Entity}{Action}` | `KafkaEntityPublisher`, `JpaEntityRepository` |

### DTO vs Domain Model

| Aspect | DTO (Infrastructure) | Domain Model |
|--------|---------------------|--------------|
| Location | Infrastructure layer | Domain layer |
| Mutability | Mutable (setters OK) | Immutable (no setters) |
| Annotations | Framework annotations OK | NO annotations |
| Validation | Optional, technical | Required, business rules |
| Behavior | None (data only) | Rich business logic |
| Purpose | Serialization/transport | Business representation |

### When to Use Domain Services

Use domain services when:
- ✅ Business logic involves **multiple entities**
- ✅ Logic doesn't naturally belong to a single entity
- ✅ You need complex calculations across entities
- ✅ You need to coordinate between domain models

Do NOT use domain services when:
- ❌ Logic belongs to a single entity (put it in the entity)
- ❌ You need infrastructure dependencies (use output ports)
- ❌ You're just orchestrating (that's application service's job)

---

## Maven Dependencies

```xml
<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <!-- Spring Boot Kafka -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-kafka</artifactId>
    </dependency>

    <!-- Lombok (infrastructure only) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- JSON Support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-json</artifactId>
    </dependency>

    <!-- Jackson -->
    <dependency>
        <groupId>tools.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>3.0.4</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Configuration

### application.yaml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: your-consumer-group
      topic: input-topic
    producer:
      topic: output-topic
```

---

## Data Flow Example

```
┌─────────────────────────────────────────────────────────────────┐
│                        External System                          │
│                      (Kafka, REST, etc.)                        │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                         │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ Input Adapter: EntityConsumer                             │ │
│  │ - Receives EntityIncomingEvent (DTO)                      │ │
│  └───────────────────┬───────────────────────────────────────┘ │
│                      │                                           │
│  ┌───────────────────▼───────────────────────────────────────┐ │
│  │ Mapper: EntityIncomingEventMapper                         │ │
│  │ - Converts DTO → YourEntity (Domain)                      │ │
│  └───────────────────┬───────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                           │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ Input Port: ProcessEntityPort.process(entity)             │ │
│  └───────────────────┬───────────────────────────────────────┘ │
│                      │                                           │
│  ┌───────────────────▼───────────────────────────────────────┐ │
│  │ Application Service: ProcessEntityService                 │ │
│  │ - Orchestrates workflow                                   │ │
│  │ - Calls domain validation                                 │ │
│  │ - Calls domain services                                   │ │
│  │ - Calls output ports                                      │ │
│  └───────────┬──────────────────────────┬────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
               │                           │
               ▼                           ▼
┌──────────────────────────┐   ┌──────────────────────────────────┐
│     DOMAIN LAYER         │   │      APPLICATION LAYER           │
│  ┌────────────────────┐  │   │  ┌────────────────────────────┐ │
│  │ Domain Model:      │  │   │  │ Output Port:               │ │
│  │ YourEntity         │  │   │  │ EntityEventPublisher       │ │
│  │ - validate()       │  │   │  └──────────┬─────────────────┘ │
│  │ - business logic   │  │   └────────────────────────────────┘
│  └────────────────────┘  │                │
│                          │                ▼
│  ┌────────────────────┐  │   ┌──────────────────────────────────┐
│  │ Domain Service:    │  │   │    INFRASTRUCTURE LAYER          │
│  │ YourDomainService  │  │   │  ┌────────────────────────────┐ │
│  │ - complex rules    │  │   │  │ Output Adapter:            │ │
│  └────────────────────┘  │   │  │ KafkaEntityPublisher       │ │
└──────────────────────────┘   │  │ - Implements output port   │ │
                               │  └──────────┬─────────────────┘ │
                               │             │                    │
                               │  ┌──────────▼─────────────────┐ │
                               │  │ Mapper:                    │ │
                               │  │ EntityOutgoingEventMapper  │ │
                               │  │ - Domain → DTO             │ │
                               │  └──────────┬─────────────────┘ │
                               └────────────────────────────────┘
                                            │
                                            ▼
                               ┌──────────────────────────┐
                               │   External System        │
                               │   (Kafka output topic)   │
                               └──────────────────────────┘
```

---

## Testing Strategy

### Domain Layer Tests
- Pure unit tests (no Spring context)
- Test business logic in isolation
- Fast execution

```java
class YourEntityTest {
    @Test
    void shouldValidateSuccessfully() {
        YourEntity entity = new YourEntity("1", "Test", valueObject);
        assertDoesNotThrow(() -> entity.validate());
    }

    @Test
    void shouldCalculatePriorityScore() {
        YourEntity entity = new YourEntity("1", "VIP-Test", largeValueObject);
        assertEquals(70, entity.calculatePriorityScore());
    }
}
```

### Application Layer Tests
- Use mocked output ports
- Verify orchestration logic
- Use Spring context if needed

```java
@ExtendWith(MockitoExtension.class)
class ProcessEntityServiceTest {
    @Mock
    private EntityRepository entityRepository;

    @Mock
    private EntityEventPublisher entityEventPublisher;

    @InjectMocks
    private ProcessEntityService service;

    @Test
    void shouldProcessEntitySuccessfully() {
        YourEntity entity = createValidEntity();

        service.process(entity);

        verify(entityRepository).save(entity);
        verify(entityEventPublisher).publish(entity);
    }
}
```

### Infrastructure Layer Tests
- Test adapters with actual infrastructure (testcontainers)
- Integration tests with Spring Boot Test

```java
@SpringBootTest
@EmbeddedKafka
class EntityConsumerIntegrationTest {
    @Autowired
    private KafkaTemplate<String, EntityIncomingEvent> kafkaTemplate;

    @Test
    void shouldConsumeAndProcessMessage() {
        EntityIncomingEvent event = createTestEvent();
        kafkaTemplate.send("input-topic", event);

        // Verify processing
    }
}
```

---

## Benefits of This Architecture

### ✅ Testability
- Domain logic testable without Spring or Kafka
- Application services testable with mocked ports
- Adapters testable independently

### ✅ Maintainability
- Clear separation of concerns
- Business logic isolated from infrastructure
- Changes to infrastructure don't affect domain

### ✅ Flexibility
- Easy to swap Kafka for RabbitMQ or other messaging
- Can add REST endpoints alongside Kafka consumers
- Multiple adapters can share the same ports

### ✅ Technology Independence
- Domain has no framework coupling
- Can migrate frameworks without rewriting business logic
- Infrastructure changes isolated to adapter layer

### ✅ Team Collaboration
- Teams can work on different layers independently
- Clear contracts via ports
- Reduced merge conflicts

---

## Common Pitfalls to Avoid

### ❌ DON'T: Put business logic in adapters
```java
// BAD - Business logic in adapter
@Component
public class EntityConsumer {
    public void consume(EntityEvent event) {
        if (event.getAmount() > 1000) { // Business logic!
            // ...
        }
    }
}
```

### ✅ DO: Keep adapters thin, delegate to domain
```java
// GOOD - Adapter delegates to domain
@Component
public class EntityConsumer {
    public void consume(EntityEvent event) {
        YourEntity domain = mapper.toDomain(event);
        port.process(domain); // Domain decides
    }
}
```

### ❌ DON'T: Pass DTOs to domain layer
```java
// BAD - Passing DTO to application layer
port.process(entityEvent); // entityEvent is a DTO!
```

### ✅ DO: Convert to domain models at adapter boundary
```java
// GOOD - Convert DTO to domain model
YourEntity domain = mapper.toDomain(entityEvent);
port.process(domain); // domain is a domain model
```

### ❌ DON'T: Add Spring annotations to domain
```java
// BAD - Spring annotations in domain
@Component // NO!
public class YourEntity {
    @Autowired // NO!
    private SomeService service;
}
```

### ✅ DO: Keep domain pure Java
```java
// GOOD - Pure Java domain
public class YourEntity {
    private final String field;

    public YourEntity(String field) {
        this.field = field;
    }
}
```

---

## Summary Checklist

When implementing hexagonal architecture, ensure:

- [ ] Domain layer has **ZERO** framework dependencies
- [ ] Domain models are **immutable** (final fields, no setters)
- [ ] Domain models contain **business logic**, not just data
- [ ] Domain services are **stateless** and framework-agnostic
- [ ] Input ports define **use cases** (what app can do)
- [ ] Output ports define **dependencies** (what app needs)
- [ ] Application services **orchestrate**, don't contain business logic
- [ ] Adapters are **thin** wrappers around technical concerns
- [ ] DTOs are used **only in infrastructure** layer
- [ ] Mappers convert between **DTOs ↔ Domain models** at boundaries
- [ ] Dependencies flow **inward only** (Infrastructure → Application → Domain)
- [ ] Domain services are wired in **application/config**, not domain layer
- [ ] Tests are **layered** (unit tests for domain, integration tests for infrastructure)

---

## References

- **Hexagonal Architecture**: Alistair Cockburn
- **Clean Architecture**: Robert C. Martin
- **Domain-Driven Design**: Eric Evans
- **Example Project**: This codebase (pmd-proposal-client-consumer)

---

**Generated from**: pmd-proposal-client-consumer project
**Date**: 2026-02-16

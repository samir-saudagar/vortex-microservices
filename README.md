# VORTEX - Enterprise Baggage Handling System

VORTEX is a high-throughput, event-driven Baggage Handling System (BHS) designed to simulate modern airport logistics. Built on a strict Domain-Driven Design (DDD) architecture, it isolates core business domains across independent microservices and utilizes asynchronous event streaming for real-time baggage routing.

> **Note:** This project is currently in active development. The current focus is strictly on the backend event-driven architecture and data pipelines. The frontend UI and End-to-End tests are planned for a future release.

## Architecture Highlights
* **Domain-Driven Microservices:** A 4-module backend isolating Check-In, Flight Info, Routing, and Equipment logic.
* **Transactional Outbox Pattern:** Guarantees data consistency between PostgreSQL databases and Kafka using Debezium Change Data Capture (CDC).
* **Fault Tolerance & Resilience:** Implements Resilience4j Circuit Breakers to gracefully handle external API failures, utilizing Redis for fallback caching.
* **Strict Binary Contracts:** Secures inter-service communication using Apache Avro and Confluent Schema Registry.
* **Idempotent Consumers:** Employs Redis-backed distributed locks to prevent duplicate event processing across consumer groups.
* **Database-per-Service:** 100% domain isolation utilizing independent PostgreSQL instances to eliminate cross-boundary data leakage.
* **Containerized Infrastructure:** One-click local environment parity using Podman/Docker Compose.

## Tech Stack
**Backend Data Pipeline:**
* Java 25 & Spring Boot 4.1
* Apache Kafka (KRaft Mode) & Confluent Schema Registry
* Debezium (PostgreSQL CDC)
* Apache Avro

**Data & Caching:**
* PostgreSQL (Independent databases per microservice)
* Redis (Idempotency locks & Circuit Breaker fallbacks)
* Spring Data JPA & Hibernate

**Infrastructure & Tooling:**
* Gradle (Kotlin DSL) for multi-module dependency management
* Podman / Docker Compose
* Resilience4j

**Frontend (Upcoming):**
* React 19 & JavaScript
* Vite for ultra-fast HMR and API proxying

## Project Roadmap / Status
- [x] **Phase 1: Infrastructure & Data Ingestion** (Check-In Service, Debezium CDC, Outbox Pattern)
- [x] **Phase 2: Resilient Processing** (Flight Info Service, Redis Idempotency, API Circuit Breakers, Avro Contracts)
- [ ] **Phase 3: Event Routing** (Routing Service & Equipment Service)
- [ ] **Phase 4: User Interface** (React Dashboard)

## Quick Start (Local Development)

### 1. Start the Infrastructure
A local bash script is provided to instantly spin up the containerized Kafka broker, Schema Registry, Redis, and PostgreSQL databases.
```bash
./dev-up.sh
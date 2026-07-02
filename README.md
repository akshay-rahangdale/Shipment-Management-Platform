# Real-Time Predictive Shipment Management Platform

An enterprise-grade, distributed, event-driven microservices architecture engineered to ingest high-frequency carrier tracking scans, manage transactional shipment workflows, and run real-time predictive anomaly and SLA breach detection.

---

## 1. Project Motive & Core Brief

### The Core Business Problem
Traditional logistics and transportation platforms handle tracking status updates using rigid, synchronous, and purely reactive patterns. If a package is stalled at customs, delayed by adverse weather, or undergoes extensive processing gaps, legacy systems typically log the issue *after the fact*. This results in missed Service Level Agreements (SLAs), unexpected financial penalties, and defensive customer service strategies where users notice delivery delays before operations does.

### The Technical Objective
This platform demonstrates a high-performance, reactive system that handles extreme write throughput without risking data consistency or locking user processing threads. 
* **Decoupled Event Fabric:** Replaces blocking, synchronous inter-service REST dependencies with asynchronous event streams managed by **Apache Kafka**. If downstream engines degrade or experience traffic surges, incoming shipment checkpoint operations remain completely unaffected.
* **Polyglot Persistence Strategy:** Applies the absolute best database model for each sub-domain capability rather than relying on a single database layout:
  * **Relational Store (PostgreSQL):** Governs strict structural entities like Customers, Carriers, baseline Shipment contracts, and financial metadata where transactional integrity (ACID) is non-negotiable.
  * **Document Store (MongoDB):** Powers high-volume, structural, and unpredictable checkpoint timelines that change shape dynamically depending on the regional carrier.
  * **Distributed Cache (Redis):** Serves as an in-memory rate limiter at the gateway, state cache for low-latency status checks, and a time-sorted index for packets approaching dangerous SLA timelines.
* **Polyglot Microservices (Java + Python ML):** Combines the type safety, performance, and concurrency management of **Spring Boot** with the statistical libraries of **Python Flask**. By integrating Python directly into the Kafka stream network, the machine learning component scores live tracking trajectories concurrently without introducing heavy REST performance footprints.

---

## 2. System Architecture

The ecosystem relies on an **API Ingestion Gateway**, a decoupled async **Message Backbone**, and specialized background processing worker nodes.

```mermaid
graph TD
    %% Entry Point
    Client[Client / Carriers] -->|1. HTTP REST API| Gateway[API Gateway <br/> Spring Cloud Gateway]

    %% Main Services
    Gateway -->|2. Route Requests| SS[Shipment Service <br/> PostgreSQL / Core Orders]
    Gateway -->|2. Route Requests| TS[Tracking Service <br/> MongoDB / Unstructured Logs]

    %% Event Bus & Messaging Backbone
    SS -->|3a. Publish Creation| Kafka{Apache Kafka <br/> Event Backbone}
    TS -->|3b. Publish Updates| Kafka

    %% Asynchronous Processing Engines
    Kafka -->|4a. Analyze Streams| ML[ML Detector <br/> Python Anomaly Service]
    Kafka -->|4b. Poll & Verify| SM[SLA Monitor <br/> Real-time Cache Set]
    Kafka -->|4c. Process Alerts| NS[Notification Service <br/> Email / SMS Gateway]

    %% ML Feedback Loop
    ML -->|5. Flag Anomalies| Kafka
    
    %% Shared Cache
    SS <--> RDS[(Redis Cache)]
    TS <--> RDS
    SM <--> RDS

    %% Styling
    style Gateway fill:#f9f,stroke:#333,stroke-width:1.5px
    style Kafka fill:#ff9,stroke:#333,stroke-width:2px
    style RDS fill:#dfd,stroke:#333,stroke-width:1.5px

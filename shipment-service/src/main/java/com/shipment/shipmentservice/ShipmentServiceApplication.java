package com.shipment.shipmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * WHAT IS THIS CLASS?
 *
 * This is the front door of the entire application.
 * When you run `java -jar shipment-service.jar`, Java starts here.
 *
 * @SpringBootApplication is actually THREE annotations bundled into one:
 *
 *   1. @Configuration
 *      Marks this class as a source of Spring "beans" (managed objects).
 *      Beans are objects Spring creates, wires together, and manages
 *      the lifecycle of. You never call `new ShipmentService()` —
 *      Spring does that for you and injects it wherever needed.
 *
 *   2. @EnableAutoConfiguration
 *      The magic of Spring Boot. Spring scans your classpath and says:
 *      "Oh, I see postgresql.jar is present — I should set up a
 *       DataSource automatically."
 *      "Oh, I see spring-kafka.jar — I should set up KafkaTemplate."
 *      Without this, you'd write hundreds of lines of @Bean setup code.
 *
 *   3. @ComponentScan
 *      Tells Spring: "scan this package and all sub-packages for classes
 *      annotated with @Service, @Repository, @Controller, @Component."
 *      That's how Spring finds your code and wires it together.
 *      If your class is OUTSIDE the base package, Spring won't find it.
 */
@SpringBootApplication

/**
 * @EnableCaching: activates the caching layer.
 * Without this annotation, @Cacheable on your methods does NOTHING —
 * Spring just executes the method every time as if the annotation wasn't there.
 * With it, Spring wraps annotated methods: check cache first, only call
 * the real method on a cache miss, then store the result.
 */
@EnableCaching

/**
 * @EnableKafka: activates @KafkaListener processing.
 * Without this, methods annotated with @KafkaListener won't subscribe
 * to any Kafka topics — they're just regular methods.
 * With it, Spring creates background threads that continuously poll
 * Kafka brokers for new messages and invoke your listener methods.
 */
@EnableKafka

/**
 * @EnableAsync: lets you annotate methods with @Async to run them
 * on a background thread pool instead of the caller's thread.
 * Used for non-critical work like firing domain events after a
 * DB write — so the HTTP response isn't delayed waiting for Kafka.
 */
@EnableAsync
public class ShipmentServiceApplication {

    public static void main(String[] args) {
        /**
         * SpringApplication.run() does the following in order:
         * 1. Creates the Spring ApplicationContext (the container that holds all beans)
         * 2. Runs auto-configuration (sets up DataSource, KafkaTemplate, etc.)
         * 3. Scans for @Component/@Service/@Repository/@Controller classes
         * 4. Injects dependencies (@Autowired / constructor injection)
         * 5. Runs Flyway migrations (creates/updates DB schema)
         * 6. Starts the embedded Tomcat server on the configured port
         * 7. Registers @KafkaListener consumers with the broker
         * 8. Marks the app as UP in /actuator/health
         *
         * If anything in steps 1-7 throws an exception, the app exits.
         * This fail-fast behaviour is intentional — a half-configured
         * service is worse than no service (Kubernetes will restart it).
         */
        SpringApplication.run(ShipmentServiceApplication.class, args);
    }
}

package com.shipment.trackingservice.kafka.consumer;

import com.shipment.trackingservice.kafka.event.ShipmentCreatedEvent;
import com.shipment.trackingservice.model.*;
import com.shipment.trackingservice.repository.TrackingRepository;
import com.shipment.trackingservice.service.TrackingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventConsumer {

    private final TrackingRepository trackingRepository;
    private final TrackingCacheService cacheService;

    @KafkaListener(
        topics       = "${kafka.topics.shipment-events}",
        groupId      = "${spring.kafka.consumer.group-id}",
        concurrency  = "3"
    )
    public void onShipmentEvent(
            @Payload ShipmentCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received event={} trackingNumber={} partition={} offset={}",
            event.getEventType(), event.getTrackingNumber(), partition, offset);

        try {
            handleEvent(event);
            acknowledgment.acknowledge();
            log.debug("Acknowledged offset={} partition={}", offset, partition);

        } catch (Exception ex) {
            log.error("Failed to process event trackingNumber={} error={}",
                event.getTrackingNumber(), ex.getMessage(), ex);
            // Do not acknowledge — Kafka will redeliver this message.
            // Spring's retry + dead-letter-topic config handles repeated failures.
        }
    }

    private void handleEvent(ShipmentCreatedEvent event) {
        if (!"SHIPMENT_CREATED".equals(event.getEventType())) {
            log.debug("Ignoring event type={}", event.getEventType());
            return;
        }

        if (trackingRepository.existsByShipmentId(event.getShipmentId())) {
            log.warn("Tracking record already exists for shipmentId={} — skipping (idempotent)",
                event.getShipmentId());
            return;
        }

        GeoLocation origin = GeoLocation.builder()
            .city(extractCity(event.getOriginAddress()))
            .countryCode(extractCountryCode(event.getOriginAddress()))
            .build();

        GeoLocation destination = GeoLocation.builder()
            .city(extractCity(event.getDestinationAddress()))
            .countryCode(extractCountryCode(event.getDestinationAddress()))
            .build();

        SlaInfo sla = SlaInfo.builder()
            .expectedDelivery(event.getEstimatedDelivery())
            .breachThresholdHours(3)
            .isBreached(false)
            .build();

        Checkpoint initialCheckpoint = Checkpoint.builder()
            .seq(1)
            .status(event.getStatus())
            .timestamp(event.getCreatedAt())
            .scanSource("SYSTEM")
            .description("Shipment created")
            .build();

        TrackingRecord record = TrackingRecord.builder()
            .shipmentId(event.getShipmentId())
            .trackingNumber(event.getTrackingNumber())
            .carrierCode(event.getCarrierCode())
            .currentStatus(event.getStatus())
            .origin(origin)
            .destination(destination)
            .sla(sla)
            .createdAt(LocalDateTime.now())
            .lastUpdatedAt(LocalDateTime.now())
            .build();

        record.appendCheckpoint(initialCheckpoint);

        trackingRepository.save(record);

        cacheService.cacheStatus(event.getTrackingNumber(), event.getStatus());

        log.info("Created tracking record for trackingNumber={}", event.getTrackingNumber());
    }

    private String extractCity(String address) {
        if (address == null || !address.contains(",")) return address;
        String[] parts = address.split(",");
        return parts.length > 1 ? parts[parts.length - 2].trim() : parts[0].trim();
    }

    private String extractCountryCode(String address) {
        if (address == null || !address.contains(",")) return "IN";
        String[] parts = address.split(",");
        return parts[parts.length - 1].trim().toUpperCase();
    }
}
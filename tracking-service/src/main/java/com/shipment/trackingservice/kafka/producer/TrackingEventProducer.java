package com.shipment.trackingservice.kafka.producer;

import com.shipment.trackingservice.kafka.event.TrackingUpdatedEvent;
import com.shipment.trackingservice.model.Checkpoint;
import com.shipment.trackingservice.model.TrackingRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.tracking-updates}")
    private String trackingUpdatesTopic;

    @Async
    public void publishTrackingUpdated(
            TrackingRecord record,
            String previousStatus,
            Checkpoint newCheckpoint) {

        TrackingUpdatedEvent event = TrackingUpdatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("TRACKING_UPDATED")
            .eventTimestamp(LocalDateTime.now())
            .shipmentId(record.getShipmentId())
            .trackingNumber(record.getTrackingNumber())
            .carrierCode(record.getCarrierCode())
            .previousStatus(previousStatus)
            .currentStatus(record.getCurrentStatus())
            .checkpointSeq(newCheckpoint.getSeq())
            .lat(newCheckpoint.getLocation() != null ? newCheckpoint.getLocation().getLat() : null)
            .lng(newCheckpoint.getLocation() != null ? newCheckpoint.getLocation().getLng() : null)
            .city(newCheckpoint.getLocation() != null ? newCheckpoint.getLocation().getCity() : null)
            .countryCode(newCheckpoint.getLocation() != null ? newCheckpoint.getLocation().getCountryCode() : null)
            .facilityCode(newCheckpoint.getLocation() != null ? newCheckpoint.getLocation().getFacilityCode() : null)
            .checkpointTimestamp(newCheckpoint.getTimestamp())
            .scanSource(newCheckpoint.getScanSource())
            .exceptionCode(newCheckpoint.getExceptionCode())
            .slaAtRisk(record.isSlaAtRisk(3))
            .estimatedDelivery(record.getSla() != null ? record.getSla().getExpectedDelivery() : null)
            .checkpointGapHours(record.getCheckpointGapHours())
            .build();

        kafkaTemplate.send(trackingUpdatesTopic, record.getTrackingNumber(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish TrackingUpdatedEvent trackingNumber={} error={}",
                        record.getTrackingNumber(), ex.getMessage());
                } else {
                    log.debug("Published TrackingUpdatedEvent trackingNumber={} offset={}",
                        record.getTrackingNumber(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
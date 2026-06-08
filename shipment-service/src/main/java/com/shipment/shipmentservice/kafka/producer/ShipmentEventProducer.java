package com.shipment.shipmentservice.kafka.producer;

import com.shipment.shipmentservice.kafka.event.ShipmentCreatedEvent;
import com.shipment.shipmentservice.kafka.event.ShipmentDeliveredEvent;
import com.shipment.shipmentservice.kafka.event.ShipmentUpdatedEvent;
import com.shipment.shipmentservice.model.Shipment;
import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.shipment-events}")
    private String shipmentEventsTopic;

    @Value("${kafka.topics.shipment-updates}")
    private String shipmentUpdatesTopic;

    @Async
    public void publishShipmentCreated(Shipment shipment) {
        ShipmentCreatedEvent event = ShipmentCreatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("SHIPMENT_CREATED")
            .eventTimestamp(LocalDateTime.now())
            .shipmentId(shipment.getId())
            .trackingNumber(shipment.getTrackingNumber())
            .senderId(shipment.getSender().getId())
            .senderName(shipment.getSender().getName())
            .senderEmail(shipment.getSender().getEmail())
            .recipientId(shipment.getRecipient().getId())
            .recipientName(shipment.getRecipient().getName())
            .recipientEmail(shipment.getRecipient().getEmail())
            .recipientPhone(shipment.getRecipient().getPhone())
            .carrierId(shipment.getCarrier().getId())
            .carrierCode(shipment.getCarrier().getCode())
            .carrierName(shipment.getCarrier().getName())
            .status(shipment.getStatus())
            .originAddress(shipment.getOriginAddress())
            .destinationAddress(shipment.getDestinationAddress())
            .weightKg(shipment.getWeightKg())
            .declaredValue(shipment.getDeclaredValue())
            .estimatedDelivery(shipment.getEstimatedDelivery())
            .createdAt(shipment.getCreatedAt())
            .build();

        publish(shipmentEventsTopic, shipment.getTrackingNumber(), event);
    }

    @Async
    public void publishShipmentUpdated(
            Shipment shipment,
            ShipmentStatus previousStatus,
            String updateReason) {

        boolean statusChanged = previousStatus != shipment.getStatus();

        ShipmentUpdatedEvent event = ShipmentUpdatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("SHIPMENT_UPDATED")
            .eventTimestamp(LocalDateTime.now())
            .shipmentId(shipment.getId())
            .trackingNumber(shipment.getTrackingNumber())
            .previousStatus(previousStatus)
            .currentStatus(shipment.getStatus())
            .statusChanged(statusChanged)
            .updateReason(updateReason)
            .recipientId(shipment.getRecipient().getId())
            .recipientName(shipment.getRecipient().getName())
            .recipientEmail(shipment.getRecipient().getEmail())
            .recipientPhone(shipment.getRecipient().getPhone())
            .destinationAddress(shipment.getDestinationAddress())
            .estimatedDelivery(shipment.getEstimatedDelivery())
            .slaAtRisk(shipment.isSlaAtRisk(3))
            .updatedAt(shipment.getUpdatedAt())
            .build();

        publish(shipmentUpdatesTopic, shipment.getTrackingNumber(), event);
    }

    @Async
    public void publishShipmentDelivered(Shipment shipment) {
        ShipmentDeliveredEvent event = ShipmentDeliveredEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("SHIPMENT_DELIVERED")
            .eventTimestamp(LocalDateTime.now())
            .shipmentId(shipment.getId())
            .trackingNumber(shipment.getTrackingNumber())
            .recipientId(shipment.getRecipient().getId())
            .recipientName(shipment.getRecipient().getName())
            .recipientEmail(shipment.getRecipient().getEmail())
            .recipientPhone(shipment.getRecipient().getPhone())
            .carrierId(shipment.getCarrier().getId())
            .carrierCode(shipment.getCarrier().getCode())
            .estimatedDelivery(shipment.getEstimatedDelivery())
            .actualDelivery(shipment.getActualDelivery())
            .transitDays(shipment.getTransitDays())
            .deliveredOnTime(
                shipment.getActualDelivery() != null &&
                !shipment.getActualDelivery().isAfter(shipment.getEstimatedDelivery())
            )
            .deliveredAt(shipment.getActualDelivery())
            .build();

        publish(shipmentEventsTopic, shipment.getTrackingNumber(), event);
    }

    private void publish(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic={} key={} error={}",
                    topic, key, ex.getMessage());
            } else {
                log.debug("Published event to topic={} key={} partition={} offset={}",
                    topic,
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });
    }
}
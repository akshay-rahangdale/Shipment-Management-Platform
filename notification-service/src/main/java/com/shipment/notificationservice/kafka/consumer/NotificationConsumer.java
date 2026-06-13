package com.shipment.notificationservice.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipment.notificationservice.kafka.event.AnomalyAlertEvent;
import com.shipment.notificationservice.kafka.event.ShipmentCreatedEvent;
import com.shipment.notificationservice.kafka.event.ShipmentUpdatedEvent;
import com.shipment.notificationservice.service.EmailService;
import com.shipment.notificationservice.service.IdempotencyService;
import com.shipment.notificationservice.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService       emailService;
    private final SmsService         smsService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper       objectMapper;

    @KafkaListener(
        topics      = "${kafka.topics.shipment-events}",
        groupId     = "${spring.kafka.consumer.group-id}",
        concurrency = "2"
    )
    public void onShipmentEvent(
            @Payload Map<String, Object> rawEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        String eventId   = (String) rawEvent.get("eventId");
        String eventType = (String) rawEvent.get("eventType");

        log.info("Received eventType={} eventId={} offset={}", eventType, eventId, offset);

        try {
            if (!idempotencyService.tryProcess(eventId)) {
                log.warn("Duplicate event skipped eventId={}", eventId);
                acknowledgment.acknowledge();
                return;
            }

            if ("SHIPMENT_CREATED".equals(eventType)) {
                ShipmentCreatedEvent event = objectMapper.convertValue(
                    rawEvent, ShipmentCreatedEvent.class);
                handleShipmentCreated(event);

            } else if ("SHIPMENT_DELIVERED".equals(eventType)) {
                ShipmentUpdatedEvent event = objectMapper.convertValue(
                    rawEvent, ShipmentUpdatedEvent.class);
                handleShipmentDelivered(event);

            } else {
                log.debug("Ignoring eventType={} on shipment-events topic", eventType);
            }

            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to process shipment event eventId={} error={}",
                eventId, ex.getMessage(), ex);
        }
    }

    @KafkaListener(
        topics      = "${kafka.topics.shipment-updates}",
        groupId     = "${spring.kafka.consumer.group-id}",
        concurrency = "2"
    )
    public void onShipmentUpdate(
            @Payload ShipmentUpdatedEvent event,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received ShipmentUpdatedEvent trackingNumber={} statusChanged={}",
            event.getTrackingNumber(), event.isStatusChanged());

        try {
            if (!idempotencyService.tryProcess(event.getEventId())) {
                log.warn("Duplicate update event skipped eventId={}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            if (event.isStatusChanged()) {
                emailService.sendStatusUpdate(
                    event.getRecipientEmail(),
                    event.getRecipientName(),
                    event.getTrackingNumber(),
                    event.getPreviousStatus(),
                    event.getCurrentStatus()
                );

                smsService.sendStatusUpdate(
                    event.getRecipientPhone(),
                    event.getTrackingNumber(),
                    event.getCurrentStatus()
                );
            }

            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to process update event eventId={} error={}",
                event.getEventId(), ex.getMessage(), ex);
        }
    }

    @KafkaListener(
        topics      = "${kafka.topics.anomaly-alerts}",
        groupId     = "${spring.kafka.consumer.group-id}",
        concurrency = "1"
    )
    public void onAnomalyAlert(
            @Payload AnomalyAlertEvent event,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received AnomalyAlertEvent trackingNumber={} riskLevel={}",
            event.getTrackingNumber(), event.getRiskLevel());

        try {
            if (!idempotencyService.tryProcess(event.getEventId())) {
                log.warn("Duplicate anomaly alert skipped eventId={}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            if ("HIGH".equals(event.getRiskLevel()) || "CRITICAL".equals(event.getRiskLevel())) {
                emailService.sendDelayAlert(
                    event.getRecipientEmail(),
                    event.getRecipientName(),
                    event.getTrackingNumber(),
                    event.getPredictedDelayHours(),
                    event.getEstimatedDelivery().toString()
                );

                smsService.sendDelayAlert(
                    event.getRecipientPhone(),
                    event.getTrackingNumber(),
                    event.getPredictedDelayHours()
                );
            }

            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to process anomaly alert eventId={} error={}",
                event.getEventId(), ex.getMessage(), ex);
        }
    }

    private void handleShipmentCreated(ShipmentCreatedEvent event) {
        emailService.sendShipmentCreated(
            event.getRecipientEmail(),
            event.getRecipientName(),
            event.getTrackingNumber(),
            event.getEstimatedDelivery().toString()
        );
    }

    private void handleShipmentDelivered(ShipmentUpdatedEvent event) {
        emailService.sendStatusUpdate(
            event.getRecipientEmail(),
            event.getRecipientName(),
            event.getTrackingNumber(),
            event.getPreviousStatus(),
            "DELIVERED"
        );
    }
}
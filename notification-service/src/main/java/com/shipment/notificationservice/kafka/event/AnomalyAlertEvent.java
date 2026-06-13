package com.shipment.notificationservice.kafka.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyAlertEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;

    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private Double anomalyScore;
    private String riskLevel;
    private Double predictedDelayHours;

    private LocalDateTime estimatedDelivery;
    private String lastKnownLocation;
}
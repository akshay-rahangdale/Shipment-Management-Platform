package com.shipment.trackingservice.kafka.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingUpdatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;
    private String carrierCode;

    private String previousStatus;
    private String currentStatus;

    private Integer checkpointSeq;
    private Double lat;
    private Double lng;
    private String city;
    private String countryCode;
    private String facilityCode;

    private LocalDateTime checkpointTimestamp;
    private String scanSource;
    private String exceptionCode;

    private boolean slaAtRisk;
    private LocalDateTime estimatedDelivery;
    private long checkpointGapHours;
}
package com.shipment.slamonitor.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBreachAlertEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;

    private UUID carrierId;
    private String carrierCode;

    private LocalDateTime estimatedDelivery;
    private long hoursUntilBreach;

    private BigDecimal penaltyPerHour;
    private int maxTransitDays;

    private String recipientEmail;
    private String recipientPhone;
    private String recipientName;

    private boolean alreadyBreached;
}

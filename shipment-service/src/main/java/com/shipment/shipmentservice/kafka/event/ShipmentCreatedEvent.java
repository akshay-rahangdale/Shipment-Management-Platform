package com.shipment.shipmentservice.kafka.event;

import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShipmentCreatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime eventTimestamp;

    private UUID shipmentId;
    private String trackingNumber;

    private UUID senderId;
    private String senderName;
    private String senderEmail;

    private UUID recipientId;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;

    private UUID carrierId;
    private String carrierCode;
    private String carrierName;

    private ShipmentStatus status;
    private String originAddress;
    private String destinationAddress;

    private BigDecimal weightKg;
    private BigDecimal declaredValue;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
}
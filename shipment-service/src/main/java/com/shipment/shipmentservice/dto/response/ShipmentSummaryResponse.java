package com.shipment.shipmentservice.dto.response;

import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentSummaryResponse {

    private UUID id;
    private String trackingNumber;
    private ShipmentStatus status;
    private String statusDescription;
    private String recipientName;
    private String destinationAddress;
    private LocalDateTime estimatedDelivery;
    private boolean slaAtRisk;
    private LocalDateTime createdAt;
}
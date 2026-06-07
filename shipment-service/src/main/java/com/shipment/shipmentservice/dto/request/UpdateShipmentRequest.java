package com.shipment.shipmentservice.dto.request;

import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShipmentRequest {

    private UUID recipientId;

    @Size(max = 500)
    private String destinationAddress;

    private ShipmentStatus status;

    @Size(max = 1000)
    private String specialInstructions;

    @Future
    private LocalDateTime estimatedDelivery;

    private String updateReason;
}
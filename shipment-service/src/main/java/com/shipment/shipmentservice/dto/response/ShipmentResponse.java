package com.shipment.shipmentservice.dto.response;

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
public class ShipmentResponse {

    private UUID id;
    private String trackingNumber;

    private CustomerResponse sender;
    private CustomerResponse recipient;
    private CarrierResponse carrier;

    private ShipmentStatus status;
    private String statusDescription;

    private String originAddress;
    private String destinationAddress;

    private BigDecimal weightKg;
    private BigDecimal declaredValue;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;

    private String specialInstructions;

    private boolean slaAtRisk;
    private long transitDays;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
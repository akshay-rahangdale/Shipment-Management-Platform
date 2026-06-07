package com.shipment.shipmentservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShipmentRequest {

    @NotNull
    private UUID senderId;

    @NotNull
    private UUID recipientId;

    @NotNull
    private UUID carrierId;

    @NotBlank
    private String serviceTier;

    @NotBlank
    @Size(max = 500)
    private String originAddress;

    @NotBlank
    @Size(max = 500)
    private String destinationAddress;

    @NotNull
    @DecimalMin(value = "0.001")
    @Digits(integer = 5, fraction = 3)
    private BigDecimal weightKg;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal declaredValue;

    @NotNull
    @Future
    private LocalDateTime estimatedDelivery;

    @Size(max = 1000)
    private String specialInstructions;
}
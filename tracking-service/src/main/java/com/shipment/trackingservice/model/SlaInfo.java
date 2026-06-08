package com.shipment.trackingservice.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaInfo {

    private LocalDateTime expectedDelivery;
    private Integer breachThresholdHours;
    private Boolean isBreached;
    private LocalDateTime breachDetectedAt;
}
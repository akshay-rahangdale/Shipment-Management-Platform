package com.shipment.slamonitor.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sla_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicy {

    @Id
    private UUID id;

    @Column(name = "carrier_id", nullable = false)
    private UUID carrierId;

    @Column(name = "service_tier", nullable = false)
    private String serviceTier;

    @Column(name = "max_transit_days", nullable = false)
    private Integer maxTransitDays;

    @Column(name = "penalty_per_hour", nullable = false)
    private BigDecimal penaltyPerHour;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(effectiveFrom) &&
               (effectiveTo == null || now.isBefore(effectiveTo));
    }
}

package com.shipment.shipmentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "sla_policies",
    indexes = {
        @Index(name = "idx_sla_carrier_tier", columnList = "carrier_id, service_tier")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "carrier")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String serviceTier;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    @Max(30)
    private Integer maxTransitDays;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal penaltyPerHour;

    @Column(nullable = false, updatable = false)
    @NotNull
    private LocalDateTime effectiveFrom;

    @Column
    private LocalDateTime effectiveTo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(effectiveFrom) &&
               (effectiveTo == null || now.isBefore(effectiveTo));
    }

    public boolean wouldBreachSoon(int currentTransitDays, int hoursThreshold) {
        int hoursRemaining = (maxTransitDays * 24) - (currentTransitDays * 24);
        return hoursRemaining <= hoursThreshold;
    }
}
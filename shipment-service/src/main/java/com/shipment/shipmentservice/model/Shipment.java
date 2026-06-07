package com.shipment.shipmentservice.model;

import com.shipment.shipmentservice.model.enums.ShipmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "shipments",
    indexes = {
        @Index(name = "idx_shipment_tracking_number", columnList = "tracking_number"),
        @Index(name = "idx_shipment_status", columnList = "status"),
        @Index(name = "idx_shipment_sender", columnList = "sender_id"),
        @Index(name = "idx_shipment_recipient", columnList = "recipient_id"),
        @Index(name = "idx_shipment_carrier", columnList = "carrier_id"),
        @Index(name = "idx_shipment_estimated_delivery", columnList = "estimated_delivery")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"sender", "recipient", "carrier", "events"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    @NotBlank
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    private Customer sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Customer recipient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private ShipmentStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String originAddress;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String destinationAddress;

    @Column(nullable = false, precision = 8, scale = 3)
    @NotNull
    @DecimalMin(value = "0.001")
    @Digits(integer = 5, fraction = 3)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal declaredValue;

    @Column(nullable = false, updatable = false)
    @NotNull
    private LocalDateTime estimatedDelivery;

    @Column
    private LocalDateTime actualDelivery;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "shipment",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("occurredAt ASC")
    @Builder.Default
    private List<ShipmentEvent> events = new ArrayList<>();

    public void addEvent(ShipmentEvent event) {
        events.add(event);
        event.setShipment(this);
    }

    public boolean isDelivered() {
        return this.status == ShipmentStatus.DELIVERED;
    }

    public boolean isActive() {
        return !this.status.isTerminal();
    }

    public long getTransitDays() {
        LocalDateTime start = createdAt;
        LocalDateTime end = actualDelivery != null ? actualDelivery : LocalDateTime.now();
        return java.time.Duration.between(start, end).toDays();
    }

    public boolean isSlaAtRisk(int warningHours) {
        if (status.isTerminal()) return false;
        LocalDateTime warningThreshold = estimatedDelivery.minusHours(warningHours);
        return LocalDateTime.now().isAfter(warningThreshold);
    }
}
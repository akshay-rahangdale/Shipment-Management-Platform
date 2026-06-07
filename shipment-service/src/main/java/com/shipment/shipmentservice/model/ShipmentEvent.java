package com.shipment.shipmentservice.model;

import com.shipment.shipmentservice.model.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "shipment_events",
    indexes = {
        @Index(name = "idx_event_shipment_id", columnList = "shipment_id"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_event_occurred_at", columnList = "occurred_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "shipment")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    @NotNull
    private EventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String description;

    @Column(length = 255)
    private String location;

    @Column(nullable = false, updatable = false)
    @NotNull
    private LocalDateTime occurredAt;

    @Column(nullable = false, length = 20)
    @NotBlank
    private String source;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    void setShipment(Shipment aThis) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
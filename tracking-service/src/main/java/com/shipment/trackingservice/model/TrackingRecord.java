package com.shipment.trackingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "tracking_records")
@CompoundIndexes({
    @CompoundIndex(name = "idx_sla_breach", def = "{'sla.isBreached': 1, 'sla.expectedDelivery': 1}"),
    @CompoundIndex(name = "idx_ml_risk",    def = "{'mlSignals.riskLevel': 1, 'lastUpdatedAt': -1}")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"checkpoints", "mlSignals"})
public class TrackingRecord {

    @Id
    private String id;

    @Indexed(unique = true)
    private UUID shipmentId;

    @Indexed(unique = true)
    private String trackingNumber;

    private String carrierCode;
    private String currentStatus;

    private GeoLocation origin;
    private GeoLocation destination;

    @Builder.Default
    private List<Checkpoint> checkpoints = new ArrayList<>();

    private SlaInfo sla;
    private MlSignals mlSignals;

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    public void appendCheckpoint(Checkpoint checkpoint) {
        int nextSeq = checkpoints.isEmpty() ? 1 : checkpoints.size() + 1;
        checkpoint.setSeq(nextSeq);
        checkpoints.add(checkpoint);
        this.currentStatus = checkpoint.getStatus();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public Checkpoint getLatestCheckpoint() {
        if (checkpoints.isEmpty()) return null;
        return checkpoints.get(checkpoints.size() - 1);
    }

    public boolean isSlaAtRisk(int warningHours) {
        if (sla == null || sla.getExpectedDelivery() == null) return false;
        if (Boolean.TRUE.equals(sla.getIsBreached())) return true;
        LocalDateTime threshold = sla.getExpectedDelivery().minusHours(warningHours);
        return LocalDateTime.now().isAfter(threshold);
    }

    public long getCheckpointGapHours() {
        if (checkpoints.size() < 2) return 0;
        Checkpoint latest = checkpoints.get(checkpoints.size() - 1);
        Checkpoint previous = checkpoints.get(checkpoints.size() - 2);
        return java.time.Duration.between(
            previous.getTimestamp(),
            latest.getTimestamp()
        ).toHours();
    }
}
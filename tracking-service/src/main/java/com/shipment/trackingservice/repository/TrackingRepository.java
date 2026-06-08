package com.shipment.trackingservice.repository;

import com.shipment.trackingservice.model.TrackingRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackingRepository extends MongoRepository<TrackingRecord, String> {

    Optional<TrackingRecord> findByShipmentId(UUID shipmentId);

    Optional<TrackingRecord> findByTrackingNumber(String trackingNumber);

    boolean existsByShipmentId(UUID shipmentId);

    @Query("{ 'sla.isBreached': false, 'sla.expectedDelivery': { $lte: ?0 } }")
    List<TrackingRecord> findAtRiskShipments(LocalDateTime threshold);

    @Query("{ 'mlSignals.riskLevel': { $in: ['HIGH', 'CRITICAL'] }, 'lastUpdatedAt': { $gte: ?0 } }")
    List<TrackingRecord> findHighRiskShipments(LocalDateTime since);

    @Query("{ 'shipmentId': ?0 }")
    @Update("{ '$push': { 'checkpoints': ?1 }, '$set': { 'currentStatus': ?2, 'lastUpdatedAt': ?3 } }")
    void pushCheckpoint(UUID shipmentId, Object checkpoint, String status, LocalDateTime updatedAt);

    @Query("{ 'shipmentId': ?0 }")
    @Update("{ '$set': { 'mlSignals': ?1, 'lastUpdatedAt': ?2 } }")
    void updateMlSignals(UUID shipmentId, Object mlSignals, LocalDateTime updatedAt);

    @Query("{ 'shipmentId': ?0 }")
    @Update("{ '$set': { 'sla.isBreached': true, 'sla.breachDetectedAt': ?1, 'lastUpdatedAt': ?1 } }")
    void markSlaBreached(UUID shipmentId, LocalDateTime breachDetectedAt);
}
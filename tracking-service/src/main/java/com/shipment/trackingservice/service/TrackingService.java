package com.shipment.trackingservice.service;
import jakarta.persistence.EntityNotFoundException;
import com.shipment.trackingservice.dto.request.CheckpointUpdateRequest;
import com.shipment.trackingservice.dto.response.TrackingResponse;
import com.shipment.trackingservice.kafka.producer.TrackingEventProducer;
import com.shipment.trackingservice.model.*;
import com.shipment.trackingservice.repository.TrackingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final TrackingRepository    trackingRepository;
    private final TrackingCacheService  cacheService;
    private final TrackingEventProducer eventProducer;

    public TrackingResponse getByTrackingNumber(String trackingNumber) {
        TrackingRecord record = trackingRepository
            .findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new EntityNotFoundException(
                "Tracking record not found: " + trackingNumber));
        return toResponse(record);
    }

    public TrackingResponse addCheckpoint(
            String trackingNumber,
            CheckpointUpdateRequest request) {

        TrackingRecord record = trackingRepository
            .findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new EntityNotFoundException(
                "Tracking record not found: " + trackingNumber));

        String previousStatus = record.getCurrentStatus();

        GeoLocation location = GeoLocation.builder()
            .lat(request.getLat())
            .lng(request.getLng())
            .city(request.getCity())
            .countryCode(request.getCountryCode())
            .facilityCode(request.getFacilityCode())
            .build();

        Checkpoint checkpoint = Checkpoint.builder()
            .status(request.getStatus())
            .location(location)
            .timestamp(request.getTimestamp())
            .scanSource(request.getScanSource())
            .exceptionCode(request.getExceptionCode())
            .description(request.getDescription())
            .build();

        record.appendCheckpoint(checkpoint);

        trackingRepository.pushCheckpoint(
            record.getShipmentId(),
            checkpoint,
            checkpoint.getStatus(),
            record.getLastUpdatedAt()
        );

        cacheService.cacheStatus(trackingNumber, checkpoint.getStatus());

        if (record.isSlaAtRisk(3) && record.getSla() != null) {
            long epochScore = record.getSla().getExpectedDelivery()
                .toEpochSecond(ZoneOffset.UTC);
            cacheService.addToSlaAtRisk(trackingNumber, epochScore);
        }

        eventProducer.publishTrackingUpdated(record, previousStatus, checkpoint);

        log.info("Checkpoint added trackingNumber={} status={} seq={}",
            trackingNumber, checkpoint.getStatus(), checkpoint.getSeq());

        return toResponse(record);
    }

    private TrackingResponse toResponse(TrackingRecord record) {
        List<TrackingResponse.CheckpointInfo> checkpointInfos = record.getCheckpoints()
            .stream()
            .map(cp -> TrackingResponse.CheckpointInfo.builder()
                .seq(cp.getSeq())
                .status(cp.getStatus())
                .city(cp.getLocation() != null ? cp.getLocation().getCity() : null)
                .countryCode(cp.getLocation() != null ? cp.getLocation().getCountryCode() : null)
                .timestamp(cp.getTimestamp())
                .scanSource(cp.getScanSource())
                .exceptionCode(cp.getExceptionCode())
                .description(cp.getDescription())
                .build())
            .collect(Collectors.toList());

        TrackingResponse.SlaDetails slaDetails = null;
        if (record.getSla() != null) {
            slaDetails = TrackingResponse.SlaDetails.builder()
                .expectedDelivery(record.getSla().getExpectedDelivery())
                .isBreached(Boolean.TRUE.equals(record.getSla().getIsBreached()))
                .isAtRisk(record.isSlaAtRisk(3))
                .breachDetectedAt(record.getSla().getBreachDetectedAt())
                .build();
        }

        TrackingResponse.RiskInfo riskInfo = null;
        if (record.getMlSignals() != null) {
            riskInfo = TrackingResponse.RiskInfo.builder()
                .anomalyScore(record.getMlSignals().getLastAnomalyScore())
                .riskLevel(record.getMlSignals().getRiskLevel())
                .predictedDelayHours(record.getMlSignals().getPredictedDelayHours())
                .build();
        }

        return TrackingResponse.builder()
            .shipmentId(record.getShipmentId())
            .trackingNumber(record.getTrackingNumber())
            .carrierCode(record.getCarrierCode())
            .currentStatus(record.getCurrentStatus())
            .origin(toLocationInfo(record.getOrigin()))
            .destination(toLocationInfo(record.getDestination()))
            .checkpoints(checkpointInfos)
            .sla(slaDetails)
            .riskInfo(riskInfo)
            .lastUpdatedAt(record.getLastUpdatedAt())
            .build();
    }

    private TrackingResponse.LocationInfo toLocationInfo(GeoLocation loc) {
        if (loc == null) return null;
        return TrackingResponse.LocationInfo.builder()
            .city(loc.getCity())
            .countryCode(loc.getCountryCode())
            .lat(loc.getLat())
            .lng(loc.getLng())
            .build();
    }
}
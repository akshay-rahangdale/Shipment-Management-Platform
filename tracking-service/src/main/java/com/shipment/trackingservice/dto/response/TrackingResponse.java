package com.shipment.trackingservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingResponse {

    private UUID shipmentId;
    private String trackingNumber;
    private String carrierCode;
    private String currentStatus;

    private LocationInfo origin;
    private LocationInfo destination;

    private List<CheckpointInfo> checkpoints;

    private SlaDetails sla;
    private RiskInfo riskInfo;

    private LocalDateTime lastUpdatedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LocationInfo {
        private String city;
        private String countryCode;
        private Double lat;
        private Double lng;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CheckpointInfo {
        private Integer seq;
        private String status;
        private String city;
        private String countryCode;
        private LocalDateTime timestamp;
        private String scanSource;
        private String exceptionCode;
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SlaDetails {
        private LocalDateTime expectedDelivery;
        private boolean isBreached;
        private boolean isAtRisk;
        private LocalDateTime breachDetectedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RiskInfo {
        private Double anomalyScore;
        private String riskLevel;
        private Double predictedDelayHours;
    }
}
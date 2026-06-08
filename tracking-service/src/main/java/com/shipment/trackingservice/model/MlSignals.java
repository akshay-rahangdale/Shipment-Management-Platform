package com.shipment.trackingservice.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlSignals {

    private Double lastAnomalyScore;
    private String riskLevel;
    private Double predictedDelayHours;
    private LocalDateTime alertSentAt;
    private Map<String, Object> featureSnapshot;
}
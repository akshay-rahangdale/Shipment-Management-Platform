package com.shipment.trackingservice.kafka.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipment.trackingservice.model.MlSignals;
import com.shipment.trackingservice.model.TrackingRecord;
import com.shipment.trackingservice.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnomalyAlertConsumer {

    private final TrackingRepository trackingRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics      = "${kafka.topics.anomaly-alerts}",
            groupId     = "${spring.kafka.consumer.group-id}"
    )
    public void onAnomalyAlert(@Payload Map<String, Object> rawAlert, Acknowledgment acknowledgment) {
        String trackingNumber = (String) rawAlert.get("trackingNumber");
        log.info("Received ML anomaly alert event for trackingNumber={}", trackingNumber);

        try {
            TrackingRecord record = trackingRepository.findByTrackingNumber(trackingNumber)
                    .orElse(null);

            if (record != null) {
                // Extract metrics computed by the Python ML service
                double score = ((Number) rawAlert.get("anomalyScore")).doubleValue();
                double delay = ((Number) rawAlert.get("predictedDelayHours")).doubleValue();
                String risk  = (String) rawAlert.get("riskLevel");

                // Map into the record's ML domain structure
                MlSignals signals = MlSignals.builder()
                        .lastAnomalyScore(score)
                        .predictedDelayHours(delay)
                        .riskLevel(risk)
                        .alertSentAt(LocalDateTime.now())
                        .build();

                record.setMlSignals(signals);
                trackingRepository.save(record);

                log.info("Successfully persisted ML signals to MongoDB for trackingNumber={}", trackingNumber);
            } else {
                log.warn("Tracking record not found for anomaly alert trackingNumber={}", trackingNumber);
            }

            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to update tracking record with ML anomaly metrics", ex);
            // In a production environment, throw to let the error handler route to a DLT
        }
    }
}

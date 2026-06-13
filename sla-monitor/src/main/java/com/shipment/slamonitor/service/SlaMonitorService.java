package com.shipment.slamonitor.service;

import com.shipment.slamonitor.kafka.event.SlaBreachAlertEvent;
import com.shipment.slamonitor.kafka.producer.SlaAlertProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaMonitorService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SlaAlertProducer              alertProducer;
    private final SlaEnrichmentService          enrichmentService;

    @Value("${sla.warning-hours:3}")
    private int warningHours;

    private static final String SLA_AT_RISK_KEY    = "sla:at-risk";
    private static final String ALERTED_KEY_PREFIX = "sla:alerted:";

    @Scheduled(fixedDelayString = "${sla.poll-interval-ms:60000}")
    public void checkAtRiskShipments() {
        log.debug("Running SLA breach check");

        long nowEpoch      = Instant.now().getEpochSecond();
        long thresholdEpoch = Instant.now().plusSeconds(warningHours * 3600L).getEpochSecond();

        Set<String> atRiskTrackingNumbers = redisTemplate.opsForZSet()
            .rangeByScore(SLA_AT_RISK_KEY, 0, thresholdEpoch);

        if (atRiskTrackingNumbers == null || atRiskTrackingNumbers.isEmpty()) {
            log.debug("No at-risk shipments found");
            return;
        }

        log.info("Found {} at-risk shipments", atRiskTrackingNumbers.size());

        for (String trackingNumber : atRiskTrackingNumbers) {
            try {
                processAtRiskShipment(trackingNumber, nowEpoch);
            } catch (Exception ex) {
                log.error("Failed to process at-risk shipment trackingNumber={} error={}",
                    trackingNumber, ex.getMessage());
            }
        }
    }

    private void processAtRiskShipment(String trackingNumber, long nowEpoch) {
        String alertedKey = ALERTED_KEY_PREFIX + trackingNumber;
        Boolean alreadyAlerted = redisTemplate.hasKey(alertedKey);

        if (Boolean.TRUE.equals(alreadyAlerted)) {
            log.debug("Alert already sent for trackingNumber={}", trackingNumber);
            return;
        }

        SlaBreachAlertEvent alert = enrichmentService.buildAlert(trackingNumber, nowEpoch);

        if (alert == null) {
            log.warn("Could not build alert for trackingNumber={} — removing from at-risk set",
                trackingNumber);
            redisTemplate.opsForZSet().remove(SLA_AT_RISK_KEY, trackingNumber);
            return;
        }

        alertProducer.publishSlaBreachAlert(alert);

        redisTemplate.opsForValue().set(
            alertedKey,
            "1",
            java.time.Duration.ofHours(warningHours + 1)
        );

        log.info("SLA breach alert triggered trackingNumber={} hoursUntilBreach={}",
            trackingNumber, alert.getHoursUntilBreach());
    }

    public void removeDeliveredShipment(String trackingNumber) {
        redisTemplate.opsForZSet().remove(SLA_AT_RISK_KEY, trackingNumber);
        redisTemplate.delete(ALERTED_KEY_PREFIX + trackingNumber);
        log.debug("Removed delivered shipment from SLA tracking trackingNumber={}", trackingNumber);
    }
}

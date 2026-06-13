package com.shipment.slamonitor.service;

import com.shipment.slamonitor.kafka.event.SlaBreachAlertEvent;
import com.shipment.slamonitor.model.SlaPolicy;
import com.shipment.slamonitor.repository.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaEnrichmentService {

    private final SlaPolicyRepository           slaPolicyRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate                  restTemplate;

    private static final String SLA_AT_RISK_KEY = "sla:at-risk";

    public SlaBreachAlertEvent buildAlert(String trackingNumber, long nowEpoch) {
        Double score = redisTemplate.opsForZSet()
            .score(SLA_AT_RISK_KEY, trackingNumber);

        if (score == null) return null;

        long expectedDeliveryEpoch = score.longValue();
        long hoursUntilBreach      = (expectedDeliveryEpoch - nowEpoch) / 3600;
        boolean alreadyBreached    = hoursUntilBreach < 0;

        LocalDateTime estimatedDelivery = LocalDateTime.ofEpochSecond(
            expectedDeliveryEpoch, 0, ZoneOffset.UTC);

        Map<String, Object> shipmentDetails = fetchShipmentDetails(trackingNumber);
        if (shipmentDetails == null) return null;

        UUID carrierId     = UUID.fromString((String) shipmentDetails.get("carrierId"));
        String carrierCode = (String) shipmentDetails.get("carrierCode");
        String serviceTier = (String) shipmentDetails.getOrDefault("serviceTier", "STANDARD");

        Optional<SlaPolicy> policyOpt = slaPolicyRepository
            .findActivePolicy(carrierId, serviceTier, LocalDateTime.now());

        return SlaBreachAlertEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("SLA_BREACH_ALERT")
            .eventTimestamp(LocalDateTime.now())
            .shipmentId(UUID.fromString((String) shipmentDetails.get("shipmentId")))
            .trackingNumber(trackingNumber)
            .carrierId(carrierId)
            .carrierCode(carrierCode)
            .estimatedDelivery(estimatedDelivery)
            .hoursUntilBreach(hoursUntilBreach)
            .penaltyPerHour(policyOpt.map(SlaPolicy::getPenaltyPerHour).orElse(null))
            .maxTransitDays(policyOpt.map(SlaPolicy::getMaxTransitDays).orElse(0))
            .recipientEmail((String) shipmentDetails.get("recipientEmail"))
            .recipientPhone((String) shipmentDetails.get("recipientPhone"))
            .recipientName((String) shipmentDetails.get("recipientName"))
            .alreadyBreached(alreadyBreached)
            .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchShipmentDetails(String trackingNumber) {
        try {
            return restTemplate.getForObject(
                "http://shipment-service/shipments/track/{trackingNumber}",
                Map.class,
                trackingNumber
            );
        } catch (Exception ex) {
            log.error("Failed to fetch shipment details trackingNumber={} error={}",
                trackingNumber, ex.getMessage());
            return null;
        }
    }
}

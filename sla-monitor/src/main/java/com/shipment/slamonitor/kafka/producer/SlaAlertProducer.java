package com.shipment.slamonitor.kafka.producer;

import com.shipment.slamonitor.kafka.event.SlaBreachAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaAlertProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.sla-alerts}")
    private String slaAlertsTopic;

    public void publishSlaBreachAlert(SlaBreachAlertEvent event) {
        kafkaTemplate.send(slaAlertsTopic, event.getTrackingNumber(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish SLA alert trackingNumber={} error={}",
                        event.getTrackingNumber(), ex.getMessage());
                } else {
                    log.info("SLA alert published trackingNumber={} hoursUntilBreach={}",
                        event.getTrackingNumber(), event.getHoursUntilBreach());
                }
            });
    }
}

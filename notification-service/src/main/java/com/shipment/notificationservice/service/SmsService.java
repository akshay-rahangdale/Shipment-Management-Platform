package com.shipment.notificationservice.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${notification.sms.account-sid:}")
    private String accountSid;

    @Value("${notification.sms.auth-token:}")
    private String authToken;

    @Value("${notification.sms.from-number:}")
    private String fromNumber;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @PostConstruct
    public void init() {
        if (smsEnabled && !accountSid.isEmpty()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS initialised");
        } else {
            log.warn("SMS disabled or credentials not configured — running in stub mode");
        }
    }

    @Async
    public void sendStatusUpdate(String toPhone, String trackingNumber, String status) {
        String body = "Shipment " + trackingNumber + " status: " + status +
                      ". Track at shipment-platform.com/track/" + trackingNumber;
        send(toPhone, body);
    }

    @Async
    public void sendDelayAlert(String toPhone, String trackingNumber, double delayHours) {
        String body = "⚠️ Shipment " + trackingNumber +
                      " may be delayed by ~" + String.format("%.0f", delayHours) +
                      " hours. We apologise for the inconvenience.";
        send(toPhone, body);
    }

    private void send(String toPhone, String body) {
        if (!smsEnabled) {
            log.info("[SMS STUB] to={} body={}", toPhone, body);
            return;
        }

        try {
            Message message = Message.creator(
                new PhoneNumber(toPhone),
                new PhoneNumber(fromNumber),
                body
            ).create();

            log.info("SMS sent sid={} to={}", message.getSid(), toPhone);

        } catch (Exception ex) {
            log.error("Failed to send SMS to={} error={}", toPhone, ex.getMessage());
            throw new RuntimeException("SMS send failed", ex);
        }
    }
}
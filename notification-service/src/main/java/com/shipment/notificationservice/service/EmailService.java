package com.shipment.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromAddress;

    @Async
    public void sendShipmentCreated(
            String toEmail,
            String recipientName,
            String trackingNumber,
            String estimatedDelivery) {

        String subject = "Your shipment " + trackingNumber + " is confirmed";
        String body    = buildCreatedEmailBody(recipientName, trackingNumber, estimatedDelivery);
        send(toEmail, subject, body);
    }

    @Async
    public void sendStatusUpdate(
            String toEmail,
            String recipientName,
            String trackingNumber,
            String previousStatus,
            String currentStatus) {

        String subject = "Shipment " + trackingNumber + " status update";
        String body    = buildStatusUpdateBody(
            recipientName, trackingNumber, previousStatus, currentStatus);
        send(toEmail, subject, body);
    }

    @Async
    public void sendDelayAlert(
            String toEmail,
            String recipientName,
            String trackingNumber,
            double predictedDelayHours,
            String estimatedDelivery) {

        String subject = "⚠️ Shipment " + trackingNumber + " may be delayed";
        String body    = buildDelayAlertBody(
            recipientName, trackingNumber, predictedDelayHours, estimatedDelivery);
        send(toEmail, subject, body);
    }

    private void send(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email sent to={} subject={}", toEmail, subject);

        } catch (Exception ex) {
            log.error("Failed to send email to={} subject={} error={}",
                toEmail, subject, ex.getMessage());
            throw new RuntimeException("Email send failed", ex);
        }
    }

    private String buildCreatedEmailBody(
            String name, String trackingNumber, String estimatedDelivery) {
        return """
            <html><body>
            <h2>Hi %s,</h2>
            <p>Your shipment has been created successfully.</p>
            <p><strong>Tracking Number:</strong> %s</p>
            <p><strong>Estimated Delivery:</strong> %s</p>
            <p>Track your shipment at any time using your tracking number.</p>
            </body></html>
            """.formatted(name, trackingNumber, estimatedDelivery);
    }

    private String buildStatusUpdateBody(
            String name, String trackingNumber,
            String previousStatus, String currentStatus) {
        return """
            <html><body>
            <h2>Hi %s,</h2>
            <p>Your shipment status has been updated.</p>
            <p><strong>Tracking Number:</strong> %s</p>
            <p><strong>Previous Status:</strong> %s</p>
            <p><strong>Current Status:</strong> %s</p>
            </body></html>
            """.formatted(name, trackingNumber, previousStatus, currentStatus);
    }

    private String buildDelayAlertBody(
            String name, String trackingNumber,
            double predictedDelayHours, String estimatedDelivery) {
        return """
            <html><body>
            <h2>Hi %s,</h2>
            <p>Our system has detected a potential delay for your shipment.</p>
            <p><strong>Tracking Number:</strong> %s</p>
            <p><strong>Predicted Delay:</strong> %.1f hours</p>
            <p><strong>Original Estimated Delivery:</strong> %s</p>
            <p>We apologise for the inconvenience. Our team is working to resolve this.</p>
            </body></html>
            """.formatted(name, trackingNumber, predictedDelayHours, estimatedDelivery);
    }
}
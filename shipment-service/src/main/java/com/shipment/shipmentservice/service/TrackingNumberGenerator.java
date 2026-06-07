package com.shipment.shipmentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.shipment.shipmentservice.repository.ShipmentRepository;

import java.security.SecureRandom;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TrackingNumberGenerator {

    private static final String PREFIX      = "SHP";
    private static final String CHARS       = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int    RANDOM_LENGTH = 10;
    private static final int    MAX_ATTEMPTS  = 10;

    private final SecureRandom random = new SecureRandom();
    private final ShipmentRepository shipmentRepository;

    public String generate() {
        return generateUnique(() -> buildCandidate());
    }

    private String generateUnique(Supplier<String> candidateSupplier) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String candidate = candidateSupplier.get();
            if (!shipmentRepository.existsByTrackingNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
            "Failed to generate unique tracking number after " + MAX_ATTEMPTS + " attempts");
    }

    private String buildCandidate() {
        StringBuilder sb = new StringBuilder(PREFIX.length() + 1 + RANDOM_LENGTH);
        sb.append(PREFIX).append("-");
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
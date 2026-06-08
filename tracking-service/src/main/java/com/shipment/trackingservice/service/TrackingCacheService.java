package com.shipment.trackingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String STATUS_KEY_PREFIX = "shipment:status:";
    private static final Duration STATUS_TTL      = Duration.ofMinutes(5);

    public void cacheStatus(String trackingNumber, String status) {
        String key = STATUS_KEY_PREFIX + trackingNumber;
        redisTemplate.opsForValue().set(key, status, STATUS_TTL);
        log.debug("Cached status trackingNumber={} status={}", trackingNumber, status);
    }

    public String getCachedStatus(String trackingNumber) {
        return redisTemplate.opsForValue().get(STATUS_KEY_PREFIX + trackingNumber);
    }

    public void evictStatus(String trackingNumber) {
        redisTemplate.delete(STATUS_KEY_PREFIX + trackingNumber);
    }

    public void addToSlaAtRisk(String trackingNumber, long expectedDeliveryEpoch) {
        redisTemplate.opsForZSet().add("sla:at-risk", trackingNumber, expectedDeliveryEpoch);
    }

    public void removeFromSlaAtRisk(String trackingNumber) {
        redisTemplate.opsForZSet().remove("sla:at-risk", trackingNumber);
    }
}
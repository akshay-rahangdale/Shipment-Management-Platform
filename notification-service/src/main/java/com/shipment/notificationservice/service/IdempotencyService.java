package com.shipment.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String  KEY_PREFIX = "notif:processed:";
    private static final Duration TTL       = Duration.ofHours(24);

    public boolean isAlreadyProcessed(String eventId) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey(KEY_PREFIX + eventId)
        );
    }

    public void markAsProcessed(String eventId) {
        redisTemplate.opsForValue().set(
            KEY_PREFIX + eventId,
            "1",
            TTL
        );
    }

    public boolean tryProcess(String eventId) {
        Boolean set = redisTemplate.opsForValue().setIfAbsent(
            KEY_PREFIX + eventId,
            "1",
            TTL
        );
        return Boolean.TRUE.equals(set);
    }
}
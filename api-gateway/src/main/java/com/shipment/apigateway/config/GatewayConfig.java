package com.shipment.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Forwarded-For");

            if (ip == null || ip.isEmpty()) {
                ip = exchange.getRequest()
                    .getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            }

            String clientIp = ip.split(",")[0].trim();
            return Mono.just(clientIp);
        };
    }
}
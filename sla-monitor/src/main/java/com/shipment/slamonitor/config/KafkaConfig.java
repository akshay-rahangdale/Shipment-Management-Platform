package com.shipment.slamonitor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.sla-alerts}")
    private String slaAlertsTopic;

    @Bean
    public NewTopic slaAlertsTopic() {
        return TopicBuilder.name(slaAlertsTopic)
            .partitions(3)
            .replicas(3)
            .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L))
            .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,     StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,   JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG,                     "all");
        config.put(ProducerConfig.RETRIES_CONFIG,                  3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,       true);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS,           false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

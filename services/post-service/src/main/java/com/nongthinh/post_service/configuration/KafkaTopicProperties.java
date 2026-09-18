package com.nongthinh.post_service.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "messaging.kafka.topics")
public record KafkaTopicProperties(@NotBlank String postEngagement) {
}

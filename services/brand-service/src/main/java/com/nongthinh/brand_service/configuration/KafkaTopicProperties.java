package com.nongthinh.brand_service.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "messaging.kafka.topics")
public class KafkaTopicProperties {
    @NotBlank private String brandProfileCreated;
    @NotBlank private String brandDocumentsRequested;
    @NotBlank private String brandNeedsRevision;
    @NotBlank private String brandProfileApproved;
    @NotBlank private String brandProfileRejected;
    @NotBlank private String brandDocumentsSubmitted;
}

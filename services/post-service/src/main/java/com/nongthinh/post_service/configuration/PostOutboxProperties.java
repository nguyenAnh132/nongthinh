package com.nongthinh.post_service.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("post-outbox")
public record PostOutboxProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("post.engagement.v1") @NotBlank String topic,
        @DefaultValue("50") @Min(1) int batchSize
) {
}

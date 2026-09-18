package com.nongthinh.post_service.configuration;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("post-outbox")
public record PostOutboxProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("50") @Min(1) int batchSize
) {
}

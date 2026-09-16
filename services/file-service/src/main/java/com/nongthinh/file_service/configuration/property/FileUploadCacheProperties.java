package com.nongthinh.file_service.configuration.property;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "caching")
public class FileUploadCacheProperties {
    @Min(1)
    private int fileUploadPolicyTtlSeconds = 60;
}

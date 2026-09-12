package com.nongthinh.auth_service.configuration.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "caching")
public class SystemParamCacheProperties {
    private long systemParamTtlSeconds = 120;
}

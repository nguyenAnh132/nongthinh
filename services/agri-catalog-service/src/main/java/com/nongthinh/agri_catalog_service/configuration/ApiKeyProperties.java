package com.nongthinh.agri_catalog_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.security.api-key")
public class ApiKeyProperties {

    private String headerName = "X-API-KEY";
    private String currentServiceKey;
}

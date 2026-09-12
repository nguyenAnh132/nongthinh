package com.nongthinh.auth_service.configuration.property;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    KeycloakProperties.class,
    SystemParamCacheProperties.class,
    KeycloakCacheProperties.class
})
public class PropertyConfig {

}

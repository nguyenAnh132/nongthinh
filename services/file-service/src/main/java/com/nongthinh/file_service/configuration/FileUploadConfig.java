package com.nongthinh.file_service.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.file_service.configuration.property.FileUploadCacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.nongthinh.file_service.infra.client.boportal")
@EnableConfigurationProperties(FileUploadCacheProperties.class)
public class FileUploadConfig {
    @Bean
    public ObjectMapper redisObjectMapper() {
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}

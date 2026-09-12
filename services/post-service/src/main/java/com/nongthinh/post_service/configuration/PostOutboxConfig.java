package com.nongthinh.post_service.configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
@Configuration
@EnableScheduling
@org.springframework.boot.context.properties.EnableConfigurationProperties(PostOutboxProperties.class)
public class PostOutboxConfig {}

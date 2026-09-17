package com.nongthinh.auth_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class BrandAccessKafkaConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> brandAccessKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            @org.springframework.beans.factory.annotation.Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setAutoStartup(autoStartup);
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD);
        var errors = new DefaultErrorHandler(new FixedBackOff(5000L, FixedBackOff.UNLIMITED_ATTEMPTS));
        errors.addNotRetryableExceptions(IllegalArgumentException.class);
        factory.setCommonErrorHandler(errors);
        return factory;
    }
}

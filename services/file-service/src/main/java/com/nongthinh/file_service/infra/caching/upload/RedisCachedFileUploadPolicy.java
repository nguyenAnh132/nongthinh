package com.nongthinh.file_service.infra.caching.upload;

import java.time.Duration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.nongthinh.file_service.application.port.out.FileUploadPolicyProvider;
import com.nongthinh.file_service.configuration.property.FileUploadCacheProperties;
import com.nongthinh.file_service.domain.file.FileUploadPolicy;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.infra.caching.RedisStringCache;

@Component
@Primary
@Slf4j
public class RedisCachedFileUploadPolicy implements FileUploadPolicyProvider {
    private final FileUploadPolicyProvider provider;
    private final RedisStringCache cache;
    private final FileUploadCacheProperties properties;
    private final ObjectMapper objectMapper;

    public RedisCachedFileUploadPolicy(
            @Qualifier("fileUploadPolicyProviderImpl") FileUploadPolicyProvider provider,
            RedisStringCache cache, FileUploadCacheProperties properties,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.provider = provider;
        this.cache = cache;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public FileUploadPolicy getPolicy(FilePurpose purpose) {
        String key = FileUploadPolicyCacheKeys.policy(purpose);
        var cached = cache.get(key);
        if (cached.isPresent()) {
            try {
                FileUploadPolicy policy = objectMapper.readValue(cached.get(), FileUploadPolicy.class);
                if (policy != null) {
                    return policy;
                }
            } catch (JsonProcessingException | IllegalArgumentException ex) {
                log.warn("[Infra - UploadPolicy] Invalid cached policy, refetching | purpose={}", purpose);
            }
            cache.invalidate(key);
        }
        FileUploadPolicy policy = provider.getPolicy(purpose);
        try {
            cache.put(key, objectMapper.writeValueAsString(policy),
                    Duration.ofSeconds(properties.getFileUploadPolicyTtlSeconds()));
        } catch (JsonProcessingException ex) {
            log.warn("[Infra - UploadPolicy] Policy serialization failed | purpose={}", purpose);
        }
        return policy;
    }
}

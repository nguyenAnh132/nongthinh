package com.nongthinh.file_service.infra.caching.upload;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.nongthinh.file_service.application.port.out.FileUploadPolicyProvider;
import com.nongthinh.file_service.configuration.property.FileUploadCacheProperties;
import com.nongthinh.file_service.domain.file.FileUploadPolicy;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.infra.caching.RedisStringCache;

class RedisCachedFileUploadPolicyTest {
    private final FileUploadPolicyProvider delegate = mock(FileUploadPolicyProvider.class);
    private final RedisStringCache cache = mock(RedisStringCache.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final FileUploadCacheProperties properties = new FileUploadCacheProperties();
    private final RedisCachedFileUploadPolicy provider =
            new RedisCachedFileUploadPolicy(delegate, cache, properties, mapper);
    private final String key = FileUploadPolicyCacheKeys.policy(FilePurpose.AVATAR);

    @Test
    void cacheHitDoesNotCallBoAndPreservesDisabledTypes() throws Exception {
        FileUploadPolicy policy = new FileUploadPolicy(100, Set.of());
        when(cache.get(key)).thenReturn(Optional.of(mapper.writeValueAsString(policy)));
        assertEquals(policy, provider.getPolicy(FilePurpose.AVATAR));
        verifyNoInteractions(delegate);
    }

    @Test
    void missFetchesPolicyAndCachesForOneMinute() throws Exception {
        FileUploadPolicy policy = new FileUploadPolicy(100, Set.of("image/jpeg"));
        when(cache.get(key)).thenReturn(Optional.empty());
        when(delegate.getPolicy(FilePurpose.AVATAR)).thenReturn(policy);
        assertEquals(policy, provider.getPolicy(FilePurpose.AVATAR));
        verify(cache).put(key, mapper.writeValueAsString(policy), Duration.ofMinutes(1));
    }

    @Test
    void configuredTtlIsUsed() throws Exception {
        properties.setFileUploadPolicyTtlSeconds(15);
        FileUploadPolicy policy = new FileUploadPolicy(100, Set.of("image/png"));
        when(cache.get(key)).thenReturn(Optional.empty());
        when(delegate.getPolicy(FilePurpose.AVATAR)).thenReturn(policy);
        provider.getPolicy(FilePurpose.AVATAR);
        verify(cache).put(key, mapper.writeValueAsString(policy), Duration.ofSeconds(15));
    }

    @Test
    void malformedOrInvalidCachedPolicyIsInvalidatedAndRefetched() {
        FileUploadPolicy policy = new FileUploadPolicy(100, Set.of("image/png"));
        when(delegate.getPolicy(FilePurpose.AVATAR)).thenReturn(policy);
        for (String json : new String[] {"broken", "null", "{\"maxSizeBytes\":0,\"allowedContentTypes\":[]}",
                "{\"maxSizeBytes\":100}", "{\"maxSizeBytes\":100,\"allowedContentTypes\":[null]}"}) {
            when(cache.get(key)).thenReturn(Optional.of(json));
            assertEquals(policy, provider.getPolicy(FilePurpose.AVATAR));
        }
        verify(cache, times(5)).invalidate(key);
        verify(delegate, times(5)).getPolicy(FilePurpose.AVATAR);
    }
}

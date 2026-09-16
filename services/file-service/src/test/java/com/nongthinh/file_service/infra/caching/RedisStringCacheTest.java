package com.nongthinh.file_service.infra.caching;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisStringCacheTest {
    @Test
    void redisOutageDoesNotPreventPolicyLookupOrReturn() {
        var template = mock(StringRedisTemplate.class);
        when(template.opsForValue()).thenThrow(new RedisConnectionFailureException("unavailable"));
        when(template.delete("key")).thenThrow(new RedisConnectionFailureException("unavailable"));
        var cache = new RedisStringCache(template);
        assertTrue(cache.get("key").isEmpty());
        assertDoesNotThrow(() -> cache.put("key", "value", Duration.ofMinutes(1)));
        assertDoesNotThrow(() -> cache.invalidate("key"));
    }
}

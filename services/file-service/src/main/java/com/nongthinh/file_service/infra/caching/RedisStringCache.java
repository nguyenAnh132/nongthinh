package com.nongthinh.file_service.infra.caching;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStringCache {
    private final StringRedisTemplate redisTemplate;

    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (DataAccessException ex) {
            log.warn("[Infra - Redis] Cache read failed | key={} exception={}", key, ex.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public void put(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (DataAccessException ex) {
            log.warn("[Infra - Redis] Cache write failed | key={} exception={}", key, ex.getClass().getSimpleName());
        }
    }

    public void invalidate(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException ex) {
            log.warn("[Infra - Redis] Cache invalidation failed | key={} exception={}", key, ex.getClass().getSimpleName());
        }
    }
}

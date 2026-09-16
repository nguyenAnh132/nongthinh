package com.nongthinh.auth_service.infra.caching;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        } catch (Exception ex) {
            log.warn(
                    "[Infrastructure - Redis] Failed to read from Redis | key={}, cause={}",
                    key,
                    ex.getMessage()
            );
            return Optional.empty();
        }
    }

    public void put(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception ex) {
            log.warn("[Infrastructure - Redis] Failed to write to Redis | key={}. cause={}", key, ex.getMessage());
        }
    }

    public void invalidate(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.warn("[Infrastructure - Redis] Failed to invalidate Redis | key={}. cause={}", key, ex.getMessage());
        }
    }

    public Optional<Duration> getRemainingTtl(String key) {
        try {
            Long seconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (seconds == null || seconds <= 0) {
                return Optional.empty();
            }
            return Optional.of(Duration.ofSeconds(seconds));
        } catch (Exception ex) {
            log.warn("[Infrastructure - Redis] Failed to read TTL from Redis | key={}. cause={}", key, ex.getMessage());
            return Optional.empty();
        }
    }
}

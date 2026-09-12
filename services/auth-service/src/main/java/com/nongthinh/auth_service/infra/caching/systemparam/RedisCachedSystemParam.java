package com.nongthinh.auth_service.infra.caching.systemparam;

import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.configuration.property.SystemParamCacheProperties;
import com.nongthinh.auth_service.infra.caching.RedisStringCache;

import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@Slf4j
public class RedisCachedSystemParam implements SystemParam {

    private final SystemParam systemParamDelegate;
    private final RedisStringCache redisStringCache;
    private final SystemParamCacheProperties cacheProperties;

    public RedisCachedSystemParam(
            @Qualifier("systemParamImpl") SystemParam systemParamDelegate,
            RedisStringCache redisStringCache,
            SystemParamCacheProperties cacheProperties) {
        this.systemParamDelegate = systemParamDelegate;
        this.redisStringCache = redisStringCache;
        this.cacheProperties = cacheProperties;
    }

    @Override
    public String getString(String name, String defaultValue) {
        return resolveValue(name).orElse(defaultValue);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        Optional<String> raw = resolveValue(name);
        if (raw.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.get().trim());
        } catch (NumberFormatException ex) {
            log.warn("System param {} value '{}' is not an integer, using default {}",
                    name, raw.get(), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        return resolveValue(name)
                .map(value -> Boolean.parseBoolean(value.trim()))
                .orElse(defaultValue);
    }

    private Optional<String> resolveValue(String name) {
        Optional<String> cached = redisStringCache.get(SystemParamCacheKeys.forParam(name));
        if (cached.isPresent()) {
            return cached;
        }

        Optional<String> fetched = Optional.ofNullable(systemParamDelegate.getString(name, null));
        fetched.ifPresent(value -> redisStringCache.put(
                SystemParamCacheKeys.forParam(name),
                value,
                Duration.ofSeconds(cacheProperties.getSystemParamTtlSeconds())));
        return fetched;
    }
}

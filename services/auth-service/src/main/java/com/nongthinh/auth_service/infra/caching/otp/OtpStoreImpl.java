package com.nongthinh.auth_service.infra.caching.otp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.auth_service.application.port.out.otp.OtpRecord;
import com.nongthinh.auth_service.application.port.out.otp.OtpStore;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.infra.caching.RedisStringCache;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OtpStoreImpl implements OtpStore {

    private final RedisStringCache redisStringCache;
    private final ObjectMapper objectMapper;

    public OtpStoreImpl(
            RedisStringCache redisStringCache,
            @Qualifier("eventObjectMapper") ObjectMapper objectMapper) {
        this.redisStringCache = redisStringCache;
        this.objectMapper = objectMapper;
    }

    @Override
    public void put(String email, OtpRecord record, int expireMinutes) {
        String key = OtpCacheKeys.forEmail(email);
        redisStringCache.put(key, serialize(record), toDuration(expireMinutes));
    }

    @Override
    public Optional<OtpRecord> find(String email) {
        return redisStringCache.get(OtpCacheKeys.forEmail(email))
                .flatMap(this::deserialize);
    }

    @Override
    public void incrementAttempt(String email, int expireMinutes) {
        String key = OtpCacheKeys.forEmail(email);
        find(email).ifPresent(record -> {
            Duration ttl = redisStringCache.getRemainingTtl(key)
                    .orElse(toDuration(expireMinutes));
            redisStringCache.put(key, serialize(record.withIncrementedAttempt()), ttl);
        });
    }

    @Override
    public void invalidate(String email) {
        redisStringCache.invalidate(OtpCacheKeys.forEmail(email));
    }

    private static Duration toDuration(int expireMinutes) {
        return Duration.ofMinutes(expireMinutes);
    }

    private String serialize(OtpRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException ex) {
            throw new InfrastructureException(ErrorCode.INTERNAL_ERROR, "Failed to serialize OTP record", ex);
        }
    }

    private Optional<OtpRecord> deserialize(String json) {
        try {
            return Optional.of(objectMapper.readValue(json, OtpRecord.class));
        } catch (JsonProcessingException ex) {
            throw new InfrastructureException(ErrorCode.INTERNAL_ERROR, "Failed to deserialize OTP record", ex);
        }
    }
}

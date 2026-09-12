package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.port.in.post.GetPostStatisticsUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.view.PostStatisticsBucket;
import com.nongthinh.post_service.application.view.PostStatisticsView;
import com.nongthinh.post_service.application.view.PostTimelinePointView;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostStatisticsUseCaseImpl implements GetPostStatisticsUseCase {
    private static final int MAX_BUCKETS = 400;
    private static final Duration MAX_RANGE = Duration.ofDays(370);

    private final PostRepository repository;

    @Override
    public PostStatisticsView execute(Instant from, Instant to, PostStatisticsBucket bucket,
                                      ZoneId zoneId) {
        validate(from, to, bucket, zoneId);

        Map<Instant, Long> counts = createEmptyBuckets(from, to, bucket, zoneId);
        List<Instant> creationTimes = repository.findCreatedAtBetween(from, to);
        for (Instant createdAt : creationTimes) {
            Instant bucketStart = bucketStart(createdAt, bucket, zoneId);
            if (counts.containsKey(bucketStart)) {
                counts.computeIfPresent(bucketStart, (ignored, count) -> count + 1);
            }
        }

        List<PostTimelinePointView> timeline = counts.entrySet().stream()
                .map(entry -> new PostTimelinePointView(
                        entry.getKey(),
                        nextBucket(entry.getKey(), bucket, zoneId),
                        entry.getValue()
                ))
                .toList();
        return new PostStatisticsView(
                repository.countAll(),
                creationTimes.size(),
                from,
                to,
                bucket,
                zoneId.getId(),
                repository.countByStatus(),
                timeline
        );
    }

    private static void validate(Instant from, Instant to, PostStatisticsBucket bucket,
                                 ZoneId zoneId) {
        if (from == null || to == null || bucket == null || zoneId == null
                || !from.isBefore(to) || Duration.between(from, to).compareTo(MAX_RANGE) > 0) {
            throw new IllegalArgumentException("invalid statistics range");
        }
    }

    private static Map<Instant, Long> createEmptyBuckets(
            Instant from, Instant to, PostStatisticsBucket bucket, ZoneId zoneId) {
        Map<Instant, Long> result = new LinkedHashMap<>();
        ZonedDateTime cursor = bucketCursor(from, bucket, zoneId);
        while (cursor.toInstant().isBefore(to)) {
            if (result.size() >= MAX_BUCKETS) {
                throw new IllegalArgumentException("statistics range has too many buckets");
            }
            result.put(cursor.toInstant(), 0L);
            cursor = advance(cursor, bucket);
        }
        return result;
    }

    private static Instant bucketStart(Instant value, PostStatisticsBucket bucket, ZoneId zoneId) {
        return bucketCursor(value, bucket, zoneId).toInstant();
    }

    private static ZonedDateTime bucketCursor(
            Instant value, PostStatisticsBucket bucket, ZoneId zoneId) {
        ZonedDateTime zoned = value.atZone(zoneId);
        if (bucket == PostStatisticsBucket.HOUR) {
            return zoned.truncatedTo(ChronoUnit.HOURS);
        }
        return zoned.toLocalDate().atStartOfDay(zoneId);
    }

    private static ZonedDateTime advance(ZonedDateTime value, PostStatisticsBucket bucket) {
        return bucket == PostStatisticsBucket.HOUR ? value.plusHours(1) : value.plusDays(1);
    }

    private static Instant nextBucket(
            Instant bucketStart, PostStatisticsBucket bucket, ZoneId zoneId) {
        return advance(bucketStart.atZone(zoneId), bucket).toInstant();
    }
}

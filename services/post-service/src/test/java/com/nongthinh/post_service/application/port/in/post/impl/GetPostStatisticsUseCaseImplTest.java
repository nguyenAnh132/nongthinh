package com.nongthinh.post_service.application.port.in.post.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.view.PostStatisticsBucket;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetPostStatisticsUseCaseImplTest {
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock private PostRepository repository;

    @Test
    void returnsZeroFilledHourlyTimelineInRequestedTimeZone() {
        Instant from = Instant.parse("2026-08-27T17:00:00Z");
        Instant to = Instant.parse("2026-08-28T17:00:00Z");
        when(repository.findCreatedAtBetween(from, to)).thenReturn(List.of(
                Instant.parse("2026-08-27T17:05:00Z"),
                Instant.parse("2026-08-27T17:45:00Z"),
                Instant.parse("2026-08-28T03:15:00Z")
        ));
        when(repository.countAll()).thenReturn(9L);
        when(repository.countByStatus()).thenReturn(Map.of(
                PostStatus.DRAFT, 1L,
                PostStatus.PUBLISHED, 7L,
                PostStatus.HIDDEN, 1L
        ));

        var result = new GetPostStatisticsUseCaseImpl(repository)
                .execute(from, to, PostStatisticsBucket.HOUR, VIETNAM);

        assertThat(result.totalPosts()).isEqualTo(9);
        assertThat(result.postsInRange()).isEqualTo(3);
        assertThat(result.timeline()).hasSize(24);
        assertThat(result.timeline().getFirst().bucketStart()).isEqualTo(from);
        assertThat(result.timeline().getFirst().count()).isEqualTo(2);
        assertThat(result.timeline()).extracting(point -> point.count()).contains(1L);
    }

    @Test
    void rejectsUnboundedStatisticsRanges() {
        Instant from = Instant.parse("2025-01-01T00:00:00Z");

        assertThatThrownBy(() -> new GetPostStatisticsUseCaseImpl(repository).execute(
                from,
                from.plus(Duration.ofDays(371)),
                PostStatisticsBucket.DAY,
                VIETNAM
        )).isInstanceOf(IllegalArgumentException.class);
    }
}

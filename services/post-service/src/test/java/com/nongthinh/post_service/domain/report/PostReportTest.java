package com.nongthinh.post_service.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostReportTest {
    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

    @Test
    void openReportCannotContainPartialResolutionData() {
        assertThatThrownBy(() -> PostReport.reconstruct(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ReportReason.SPAM,
                null, ReportStatus.PENDING, UUID.randomUUID(), null, null, NOW, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("resolution data");
    }

    @Test
    void failedResolutionLeavesReportPending() {
        PostReport report = PostReport.create(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), ReportReason.SPAM, null, NOW);

        assertThatThrownBy(() -> report.resolve(null, "Handled", NOW.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("moderatorId");

        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getResolvedAt()).isNull();
    }

    @Test
    void resolvesReportWithNormalizedNote() {
        UUID moderatorId = UUID.randomUUID();
        PostReport report = PostReport.create(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), ReportReason.OTHER, "  Details  ", NOW);

        report.startReview(NOW.plusSeconds(1));
        report.resolve(moderatorId, "  Handled  ", NOW.plusSeconds(2));

        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(report.getResolvedBy()).isEqualTo(moderatorId);
        assertThat(report.getResolutionNote()).isEqualTo("Handled");
    }
}

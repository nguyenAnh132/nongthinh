package com.nongthinh.post_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@EnabledIfSystemProperty(named = "post.local.integration", matches = "true")
@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/unused"
})
class LocalPostReportPersistenceTest {
    private static final UUID POST_ID = UUID.fromString("f1eb833e-55a0-4764-85fe-e2ed69ef1dbb");
    private static final UUID AUTHOR_ID = UUID.fromString("72061f1f-a76a-440b-895c-aa44dcf5adb4");
    private static final UUID REPORTER_ID = UUID.fromString("485e5f7c-9da6-41b2-8762-f5a4a728bd9d");
    private static final UUID MODERATOR_ID = UUID.fromString("dd8f3fb7-0298-4297-9c8c-7b8f66a37f8f");
    private static final UUID REPORT_ID = UUID.fromString("7c2eef83-a975-4e5e-b49e-43754c4983e7");
    private static final Instant NOW = Instant.parse("2026-08-26T00:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PostReportRepository reports;

    @Test
    @Transactional
    void persistsFiltersAndTransitionsPostReport() {
        UUID postTypeId = jdbc.queryForObject(
                "SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class
        );
        jdbc.update("""
                        INSERT INTO posts (
                            id, author_user_id, post_type_id, content, visibility, status,
                            published_at, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, 'PUBLIC', 'PUBLISHED', ?, ?, ?)
                        """,
                POST_ID,
                AUTHOR_ID,
                postTypeId,
                "Reported post",
                Timestamp.from(NOW),
                Timestamp.from(NOW),
                Timestamp.from(NOW)
        );

        PostReport report = reports.save(PostReport.create(
                REPORT_ID,
                POST_ID,
                REPORTER_ID,
                ReportReason.MISINFORMATION,
                "Incorrect farming advice",
                NOW.plusSeconds(1)
        ));

        assertThat(reports.existsByPostIdAndReporterId(POST_ID, REPORTER_ID)).isTrue();
        assertThat(reports.findAll(ReportStatus.PENDING, 0, 20).items())
                .singleElement()
                .satisfies(item -> assertThat(item.getId()).isEqualTo(REPORT_ID));

        report.startReview(NOW.plusSeconds(2));
        reports.save(report);
        report.resolve(MODERATOR_ID, "Confirmed", NOW.plusSeconds(3));
        PostReport resolved = reports.save(report);

        assertThat(resolved.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(resolved.getResolvedBy()).isEqualTo(MODERATOR_ID);
        assertThat(reports.findByIdForUpdate(REPORT_ID))
                .get()
                .satisfies(persisted -> {
                    assertThat(persisted.getId()).isEqualTo(REPORT_ID);
                    assertThat(persisted.getStatus()).isEqualTo(ReportStatus.RESOLVED);
                    assertThat(persisted.getResolvedBy()).isEqualTo(MODERATOR_ID);
                });

        assertThatThrownBy(() -> reports.save(PostReport.create(
                UUID.randomUUID(),
                POST_ID,
                REPORTER_ID,
                ReportReason.SPAM,
                null,
                NOW.plusSeconds(4)
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.POST_ALREADY_REPORTED));
    }
}

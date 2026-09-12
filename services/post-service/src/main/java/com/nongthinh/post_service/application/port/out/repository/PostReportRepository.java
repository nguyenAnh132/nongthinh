package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.application.model.PostReportPage;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import java.util.Optional;
import java.util.UUID;

public interface PostReportRepository {
    Optional<PostReport> findById(UUID id);
    Optional<PostReport> findByIdForUpdate(UUID id);
    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);
    PostReportPage findAll(ReportStatus status, int page, int size);
    PostReport save(PostReport report);
}

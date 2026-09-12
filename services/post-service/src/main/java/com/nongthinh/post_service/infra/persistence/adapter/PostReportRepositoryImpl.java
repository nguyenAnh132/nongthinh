package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.model.PostReportPage;
import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostReportEntity;
import com.nongthinh.post_service.infra.persistence.mapper.InteractionPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostReportRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class PostReportRepositoryImpl implements PostReportRepository {
    private final JpaPostReportRepository repository;
    private final InteractionPersistenceMapper mapper;

    public PostReportRepositoryImpl(JpaPostReportRepository repository, InteractionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PostReport> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PostReport> findByIdForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId) {
        return repository.existsByPostIdAndReporterId(postId, reporterId);
    }

    @Override
    public PostReportPage findAll(ReportStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"))
        );
        Page<JpaPostReportEntity> result = status == null
                ? repository.findAll(pageable)
                : repository.findAllByStatus(status.name(), pageable);
        return new PostReportPage(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    public PostReport save(PostReport value) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(value)));
        } catch (DataIntegrityViolationException exception) {
            if (hasConstraint(exception, "uq_post_reports_reporter")) {
                throw new BusinessException(ErrorCode.POST_ALREADY_REPORTED, exception);
            }
            throw exception;
        }
    }

    private static boolean hasConstraint(Throwable exception, String constraintName) {
        Throwable current = exception;
        while (current != null) {
            if (current.getMessage() != null
                    && current.getMessage().contains(constraintName)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

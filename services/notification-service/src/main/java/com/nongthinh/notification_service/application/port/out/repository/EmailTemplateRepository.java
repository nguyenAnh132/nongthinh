package com.nongthinh.notification_service.application.port.out.repository;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EmailTemplateRepository {

    List<EmailTemplate> findByPurposeId(Long purposeId);

    Optional<EmailTemplate> findById(Long id);

    Optional<EmailTemplate> findActiveByPurposeId(Long purposeId);

    EmailTemplate save(EmailTemplate template);

    void deactivateAllActiveByPurposeId(Long purposeId, Instant updatedAt);
}

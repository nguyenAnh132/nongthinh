package com.nongthinh.notification_service.infra.persistence.emailtemplate;

import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailTemplateRepositoryImpl implements EmailTemplateRepository {

    private final JpaEmailTemplateRepository jpaRepository;
    private final EmailTemplatePersistenceMapper mapper;

    @Override
    public List<EmailTemplate> findByPurposeId(Long purposeId) {
        return jpaRepository.findByPurposeIdOrderByCreatedAtDesc(purposeId).stream()
                .map(mapper::toTemplateDomain)
                .toList();
    }

    @Override
    public Optional<EmailTemplate> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toTemplateDomain);
    }

    @Override
    public Optional<EmailTemplate> findActiveByPurposeId(Long purposeId) {
        return jpaRepository.findByPurposeIdAndActiveTrue(purposeId).map(mapper::toTemplateDomain);
    }

    @Override
    public EmailTemplate save(EmailTemplate template) {
        return mapper.toTemplateDomain(jpaRepository.save(mapper.toTemplateEntity(template)));
    }

    @Override
    public void deactivateAllActiveByPurposeId(Long purposeId, Instant updatedAt) {
        jpaRepository.deactivateAllActiveByPurposeId(purposeId, updatedAt);
    }
}

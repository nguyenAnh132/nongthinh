package com.nongthinh.notification_service.infra.persistence.emailtemplate;

import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplatePurpose;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailTemplatePurposeRepositoryImpl implements EmailTemplatePurposeRepository {

    private final JpaEmailTemplatePurposeRepository jpaRepository;
    private final EmailTemplatePersistenceMapper mapper;

    @Override
    public List<EmailTemplatePurpose> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toPurposeDomain)
                .toList();
    }

    @Override
    public Optional<EmailTemplatePurpose> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toPurposeDomain);
    }

    @Override
    public Optional<EmailTemplatePurpose> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toPurposeDomain);
    }

    @Override
    public EmailTemplatePurpose save(EmailTemplatePurpose purpose) {
        return mapper.toPurposeDomain(jpaRepository.save(mapper.toPurposeEntity(purpose)));
    }
}

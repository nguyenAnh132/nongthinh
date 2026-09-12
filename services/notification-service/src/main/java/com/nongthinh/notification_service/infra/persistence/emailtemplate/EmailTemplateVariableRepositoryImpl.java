package com.nongthinh.notification_service.infra.persistence.emailtemplate;

import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateVariableRepository;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailTemplateVariableRepositoryImpl implements EmailTemplateVariableRepository {

    private final JpaEmailTemplateVariableRepository jpaRepository;
    private final EmailTemplatePersistenceMapper mapper;

    @Override
    public List<EmailTemplateVariable> findByPurposeId(Long purposeId) {
        return jpaRepository.findByPurposeIdOrderByVariableNameAsc(purposeId).stream()
                .map(mapper::toVariableDomain)
                .toList();
    }
}

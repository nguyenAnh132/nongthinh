package com.nongthinh.notification_service.infra.persistence.emailtemplate;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailTemplateVariableRepository extends JpaRepository<JpaEmailTemplateVariableEntity, Long> {

    List<JpaEmailTemplateVariableEntity> findByPurposeIdOrderByVariableNameAsc(Long purposeId);
}

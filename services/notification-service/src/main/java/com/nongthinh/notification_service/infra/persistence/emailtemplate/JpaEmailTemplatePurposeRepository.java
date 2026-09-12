package com.nongthinh.notification_service.infra.persistence.emailtemplate;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailTemplatePurposeRepository extends JpaRepository<JpaEmailTemplatePurposeEntity, Long> {

    Optional<JpaEmailTemplatePurposeEntity> findByCode(String code);
}

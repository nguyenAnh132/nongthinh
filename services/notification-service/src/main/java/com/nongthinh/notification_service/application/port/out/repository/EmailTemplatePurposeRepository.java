package com.nongthinh.notification_service.application.port.out.repository;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplatePurpose;
import java.util.List;
import java.util.Optional;

public interface EmailTemplatePurposeRepository {

    List<EmailTemplatePurpose> findAll();

    Optional<EmailTemplatePurpose> findById(Long id);

    Optional<EmailTemplatePurpose> findByCode(String code);

    EmailTemplatePurpose save(EmailTemplatePurpose purpose);
}

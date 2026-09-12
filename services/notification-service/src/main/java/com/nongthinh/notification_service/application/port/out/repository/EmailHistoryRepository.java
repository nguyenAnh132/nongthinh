package com.nongthinh.notification_service.application.port.out.repository;

import com.nongthinh.notification_service.domain.emailhistory.EmailHistory;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface EmailHistoryRepository {

    List<EmailHistory> findByPurposeId(Long purposeId);

    List<EmailHistory> findByTemplateId(Long templateId);

    List<EmailHistory> findByUserId(UUID userId);

    List<EmailHistory> findAll();

    Optional<EmailHistory> findById(Long id);

    void save(EmailHistory emailHistory);

}

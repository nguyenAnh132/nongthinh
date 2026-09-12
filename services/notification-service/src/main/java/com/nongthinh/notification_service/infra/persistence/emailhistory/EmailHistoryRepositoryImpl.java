package com.nongthinh.notification_service.infra.persistence.emailhistory;

import org.springframework.stereotype.Repository;
import com.nongthinh.notification_service.application.port.out.repository.EmailHistoryRepository;
import com.nongthinh.notification_service.domain.emailhistory.EmailHistory;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EmailHistoryRepositoryImpl implements EmailHistoryRepository {

    private final JpaEmailHistoryRepository jpaEmailHistoryRepository;
    private final EmailHistoryMapper emailHistoryMapper;

    @Override
    public List<EmailHistory> findAll() {
        return jpaEmailHistoryRepository.findAll()
            .stream()
            .map(emailHistoryMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<EmailHistory> findById(Long id) {
        return jpaEmailHistoryRepository.findById(id)
            .map(emailHistoryMapper::toDomain);
    }

    @Override
    public List<EmailHistory> findByPurposeId(Long purposeId) {
        return jpaEmailHistoryRepository.findByPurposeId(purposeId)
            .stream()
            .map(emailHistoryMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<EmailHistory> findByTemplateId(Long templateId) {
        return jpaEmailHistoryRepository.findByTemplateId(templateId)
            .stream()
            .map(emailHistoryMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<EmailHistory> findByUserId(UUID userId) {
        return jpaEmailHistoryRepository.findByUserId(userId)
            .stream()
            .map(emailHistoryMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void save(EmailHistory emailHistory) {

        JpaEmailHistoryEntity jpaEmailHistoryEntity = emailHistoryMapper.toJpaEmailHistoryEntity(emailHistory);
        jpaEmailHistoryRepository.save(jpaEmailHistoryEntity);
    }
}

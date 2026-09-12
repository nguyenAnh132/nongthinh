package com.nongthinh.notification_service.infra.persistence.emailhistory;

import org.mapstruct.Mapper;
import com.nongthinh.notification_service.domain.emailhistory.EmailHistory;

@Mapper(componentModel = "spring")
public interface EmailHistoryMapper {

    JpaEmailHistoryEntity toJpaEmailHistoryEntity(EmailHistory emailHistory);

    default EmailHistory toDomain(JpaEmailHistoryEntity jpaEmailHistoryEntity) {
        return EmailHistory.reconstruct(
            jpaEmailHistoryEntity.getId(),
            jpaEmailHistoryEntity.getUserId(),
            jpaEmailHistoryEntity.getTemplateId(),
            jpaEmailHistoryEntity.getPurposeId(),
            jpaEmailHistoryEntity.getStatus(),
            jpaEmailHistoryEntity.getSendAt()
        );
    }
}

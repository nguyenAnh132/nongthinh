package com.nongthinh.notification_service.infra.persistence.emailtemplate;

import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplatePurpose;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplateVariable;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplatePersistenceMapper {

    public EmailTemplatePurpose toPurposeDomain(JpaEmailTemplatePurposeEntity entity) {
        return EmailTemplatePurpose.reconstruct(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isSystemDefined(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public JpaEmailTemplatePurposeEntity toPurposeEntity(EmailTemplatePurpose purpose) {
        JpaEmailTemplatePurposeEntity entity = new JpaEmailTemplatePurposeEntity();
        entity.setId(purpose.getId());
        entity.setCode(purpose.getCode());
        entity.setName(purpose.getName());
        entity.setDescription(purpose.getDescription());
        entity.setSystemDefined(purpose.isSystemDefined());
        entity.setCreatedAt(purpose.getCreatedAt());
        entity.setUpdatedAt(purpose.getUpdatedAt());
        return entity;
    }

    public EmailTemplate toTemplateDomain(JpaEmailTemplateEntity entity) {
        return EmailTemplate.reconstruct(
                entity.getId(),
                entity.getPurposeId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSubject(),
                entity.getHtmlContent(),
                entity.getTextContent(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public JpaEmailTemplateEntity toTemplateEntity(EmailTemplate template) {
        JpaEmailTemplateEntity entity = new JpaEmailTemplateEntity();
        entity.setId(template.getId());
        entity.setPurposeId(template.getPurposeId());
        entity.setName(template.getName());
        entity.setDescription(template.getDescription());
        entity.setSubject(template.getSubject());
        entity.setHtmlContent(template.getHtmlContent());
        entity.setTextContent(template.getTextContent());
        entity.setActive(template.isActive());
        entity.setCreatedAt(template.getCreatedAt());
        entity.setUpdatedAt(template.getUpdatedAt());
        return entity;
    }

    public EmailTemplateVariable toVariableDomain(JpaEmailTemplateVariableEntity entity) {
        return EmailTemplateVariable.reconstruct(
                entity.getId(),
                entity.getPurposeId(),
                entity.getVariableName(),
                entity.getDescription(),
                entity.getExampleValue(),
                entity.isRequired(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

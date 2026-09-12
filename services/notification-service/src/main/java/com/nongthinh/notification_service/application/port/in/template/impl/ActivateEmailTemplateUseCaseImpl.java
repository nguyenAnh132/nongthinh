package com.nongthinh.notification_service.application.port.in.template.impl;

import com.nongthinh.notification_service.application.port.in.template.ActivateEmailTemplateUseCase;
import com.nongthinh.notification_service.application.port.out.ClockProvider;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplateRepository;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplate;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivateEmailTemplateUseCaseImpl implements ActivateEmailTemplateUseCase {

    private final EmailTemplateRepository templateRepository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(Long templateId) {
        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_TEMPLATE_NOT_FOUND));

        Instant now = clockProvider.now();
        templateRepository.deactivateAllActiveByPurposeId(template.getPurposeId(), now);
        template.activate(now);
        templateRepository.save(template);
    }
}

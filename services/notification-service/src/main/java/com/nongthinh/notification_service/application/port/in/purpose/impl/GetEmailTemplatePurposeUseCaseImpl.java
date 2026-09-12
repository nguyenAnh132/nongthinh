package com.nongthinh.notification_service.application.port.in.purpose.impl;

import com.nongthinh.notification_service.application.port.in.purpose.GetEmailTemplatePurposeUseCase;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.view.EmailTemplatePurposeView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplatePurpose;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetEmailTemplatePurposeUseCaseImpl implements GetEmailTemplatePurposeUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;

    @Override
    public EmailTemplatePurposeView execute(Long id) {

        EmailTemplatePurpose emailTemplatePurpose = purposeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_TEMPLATE_PURPOSE_NOT_FOUND));

        return EmailTemplatePurposeView.fromDomain(emailTemplatePurpose);
    }
}

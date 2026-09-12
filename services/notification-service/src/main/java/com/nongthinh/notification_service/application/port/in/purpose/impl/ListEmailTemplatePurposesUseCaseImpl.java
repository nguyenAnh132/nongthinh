package com.nongthinh.notification_service.application.port.in.purpose.impl;

import com.nongthinh.notification_service.application.port.in.purpose.ListEmailTemplatePurposesUseCase;
import com.nongthinh.notification_service.application.port.out.repository.EmailTemplatePurposeRepository;
import com.nongthinh.notification_service.application.view.EmailTemplatePurposeView;
import com.nongthinh.notification_service.domain.emailtemplate.EmailTemplatePurpose;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListEmailTemplatePurposesUseCaseImpl implements ListEmailTemplatePurposesUseCase {

    private final EmailTemplatePurposeRepository purposeRepository;

    @Override
    public List<EmailTemplatePurposeView> execute() {
        return purposeRepository.findAll().stream()
                .map(EmailTemplatePurposeView::fromDomain)
                .toList();
    }
}

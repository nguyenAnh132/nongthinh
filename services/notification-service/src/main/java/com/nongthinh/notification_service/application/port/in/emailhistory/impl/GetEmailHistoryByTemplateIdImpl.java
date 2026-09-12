package com.nongthinh.notification_service.application.port.in.emailhistory.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.notification_service.application.port.in.emailhistory.GetEmailHistoryByTemplateId;
import com.nongthinh.notification_service.application.view.EmailHistoryView;
import com.nongthinh.notification_service.application.port.out.repository.EmailHistoryRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetEmailHistoryByTemplateIdImpl implements GetEmailHistoryByTemplateId {

    private final EmailHistoryRepository emailHistoryRepository;

    @Override
    public List<EmailHistoryView> execute(Long templateId) {
        return emailHistoryRepository.findByTemplateId(templateId)
            .stream()
            .map(EmailHistoryView::fromDomain)
            .collect(Collectors.toList());
    }
}
